"""Scraper para qualitycenter.com (WordPress, server-rendered)."""

from bs4 import BeautifulSoup

from .base import Event, fetch_html, parse_fecha_larga

URL = "https://qualitycenter.com/"
FUENTE = "qualitycenter.com"


def scrape() -> list[Event]:
    html = fetch_html(URL)
    soup = BeautifulSoup(html, "html.parser")
    eventos = []

    for card in soup.select(".show-card"):
        titulo_el = card.select_one(".show-title")
        fecha_el = card.select_one(".show-date")
        link_el = card.select_one("a[href]")
        img_el = card.select_one("img")

        if not titulo_el:
            continue

        titulo = titulo_el.get_text(strip=True)
        fecha_txt = fecha_el.get_text(strip=True) if fecha_el else ""
        # fechas tipo "21 y 22 de agosto, 2026" -> nos quedamos con la primera
        fecha_txt_simple = fecha_txt.split(" y ")[0].strip()
        fecha_iso = parse_fecha_larga(fecha_txt_simple)

        imagen = None
        if img_el:
            imagen = img_el.get("data-src") or img_el.get("src")

        eventos.append(Event(
            titulo=titulo,
            venue="Quality Center",
            ciudad=None,  # TODO: confirmar ciudad real del venue
            fecha_inicio=fecha_iso,
            imagen_url=imagen,
            ticket_url=link_el.get("href") if link_el else None,
            fuente=FUENTE,
        ))

    return eventos


if __name__ == "__main__":
    for e in scrape():
        print(e.to_dict())
