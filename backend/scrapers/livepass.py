"""Scraper para livepass.com.ar (server-rendered, plataforma 'TicketPlus')."""

import re

from bs4 import BeautifulSoup

from .base import Event, fetch_html, parse_fecha_abreviada

URL = "https://livepass.com.ar/"
FUENTE = "livepass.com.ar"
BASE = "https://livepass.com.ar"


def scrape() -> list[Event]:
    html = fetch_html(URL)
    soup = BeautifulSoup(html, "html.parser")
    eventos = []

    for card in soup.select("[data-event-name]"):
        titulo = card.get("data-event-name", "").strip()
        if not titulo:
            continue

        link_el = card.select_one("a[href]")
        fecha_el = card.select_one(".date-home")
        img_el = card.select_one("img")

        fecha_txt = fecha_el.get_text(strip=True) if fecha_el else ""
        fecha_iso = parse_fecha_abreviada(fecha_txt)

        ticket_url = None
        if link_el and link_el.get("href"):
            href = link_el["href"]
            ticket_url = href if href.startswith("http") else f"{BASE}{href}"

        venue = None
        m = re.search(r"\ben\s+(.+)$", titulo, flags=re.IGNORECASE)
        if m:
            venue = m.group(1).strip()

        eventos.append(Event(
            titulo=titulo,
            venue=venue,
            fecha_inicio=fecha_iso,
            imagen_url=img_el.get("src") if img_el else None,
            ticket_url=ticket_url,
            fuente=FUENTE,
        ))

    return eventos


if __name__ == "__main__":
    for e in scrape():
        print(e.to_dict())
