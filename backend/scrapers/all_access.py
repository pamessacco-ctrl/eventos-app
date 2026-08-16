"""Scraper para allaccess.com.ar (server-rendered, plataforma 'Crowder')."""

import re

from bs4 import BeautifulSoup

from .base import Event, fetch_html, parse_fecha_larga

URL = "https://www.allaccess.com.ar/"
FUENTE = "allaccess.com.ar"


def scrape() -> list[Event]:
    html = fetch_html(URL)
    soup = BeautifulSoup(html, "html.parser")
    eventos = []

    for card in soup.select(".grid_element"):
        titulo_el = card.select_one(".item_title")
        fecha_el = card.select_one(".details p")
        link_el = card.select_one("a[href]")
        img_el = card.select_one("img")

        if not titulo_el:
            continue

        titulo = titulo_el.get_text(strip=True)
        fecha_txt = fecha_el.get_text(strip=True) if fecha_el else ""
        fecha_iso = parse_fecha_larga(fecha_txt)

        ticket_url = None
        if link_el and link_el.get("href"):
            href = link_el["href"]
            ticket_url = href if href.startswith("http") else f"https://www.allaccess.com.ar/{href.lstrip('../')}"

        imagen = None
        if img_el:
            imagen = img_el.get("src")

        # el título suele terminar en "... EN <VENUE>"
        venue = None
        m = re.search(r"\bEN\s+(.+)$", titulo, flags=re.IGNORECASE)
        if m:
            venue = m.group(1).strip().title()

        eventos.append(Event(
            titulo=titulo,
            venue=venue,
            fecha_inicio=fecha_iso,
            imagen_url=imagen,
            ticket_url=ticket_url,
            fuente=FUENTE,
        ))

    return eventos


if __name__ == "__main__":
    for e in scrape():
        print(e.to_dict())
