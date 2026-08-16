"""Scraper para movistararena.com.ar.

El sitio es Blazor Server (renderiza vía SignalR, no hay HTML útil en el
fetch crudo). Pero una vez que la app termina de montar, inyecta un bloque
<script type="application/ld+json"> con schema.org MusicEvent para SEO, con
todos los datos que necesitamos ya limpios. Usamos Playwright solo para
esperar ese render y extraer el JSON-LD.
"""

import json
import re

from playwright.sync_api import sync_playwright

from .base import Event

URL = "https://www.movistararena.com.ar/shows"
FUENTE = "movistararena.com.ar"


def _extraer_json_ld(html: str) -> dict | None:
    m = re.search(
        r'<script type="application/ld\+json">(?:<!--!-->)?(.*?)</script>',
        html, re.S,
    )
    if not m:
        return None
    return json.loads(m.group(1))


def scrape() -> list[Event]:
    with sync_playwright() as p:
        browser = p.chromium.launch()
        page = browser.new_page()
        page.goto(URL, timeout=30000, wait_until="networkidle")
        page.wait_for_timeout(1500)
        html = page.content()
        browser.close()

    data = _extraer_json_ld(html)
    if not data:
        return []

    eventos = []
    for item in data.get("itemListElement", []):
        ev = item.get("item", {})
        if ev.get("@type") != "MusicEvent":
            continue

        location = ev.get("location", {}) or {}
        address = location.get("address", {}) or {}
        offers = ev.get("offers", {}) or {}

        precio = None
        if offers.get("price") not in (None, ""):
            try:
                precio = float(offers["price"])
            except (TypeError, ValueError):
                precio = None

        eventos.append(Event(
            titulo=ev.get("name", "").strip(),
            venue=location.get("name"),
            ciudad=address.get("addressLocality"),
            fecha_inicio=ev.get("startDate"),
            precio_desde=precio,
            moneda=offers.get("priceCurrency", "ARS"),
            imagen_url=ev.get("image"),
            ticket_url=ev.get("url"),
            fuente=FUENTE,
        ))

    return eventos


if __name__ == "__main__":
    for e in scrape():
        print(e.to_dict())
