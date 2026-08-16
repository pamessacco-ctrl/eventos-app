"""Scraper para rosarioencartel.com.ar (WordPress, server-rendered).

Las fechas no traen año y a veces son rangos ("Del 13 al 16 de agosto",
"Domingos 16 y 23 de agosto"), así que usamos un parser propio en vez del
genérico de base.py: nos quedamos con el primer día del rango como
fecha_inicio.
"""

import re
import time

import requests
from bs4 import BeautifulSoup

from .base import DEFAULT_HEADERS, Event, MESES, _infer_year

URL = "https://www.rosarioencartel.com.ar/"
FUENTE = "rosarioencartel.com.ar"

# ej: "Del 13 al 16 de agosto" / "Domingos 16 y 23 de agosto" / "Miércoles 19 de agosto"
_PATRON_FECHA = re.compile(
    r"(\d{1,2})(?:\s*(?:al|y)\s*\d{1,2})?\s*de\s+([a-záéíóúñ]+)",
    re.IGNORECASE,
)


def _parse_fecha(texto: str) -> str | None:
    if not texto:
        return None
    m = _PATRON_FECHA.search(texto.strip().lower())
    if not m:
        return None
    dia, mes_txt = m.groups()
    mes = MESES.get(mes_txt)
    if not mes:
        return None
    try:
        from datetime import date
        anio = _infer_year(mes, int(dia))
        return date(anio, mes, int(dia)).isoformat()
    except ValueError:
        return None


def _fetch_con_reintentos(url: str, intentos: int = 3) -> str:
    """El hosting de este sitio es lento/inestable: a veces tarda >20s en
    responder. Reintentamos con timeouts crecientes antes de rendirnos."""
    ultimo_error = None
    for i in range(intentos):
        try:
            resp = requests.get(url, headers=DEFAULT_HEADERS, timeout=30 + i * 20)
            resp.raise_for_status()
            return resp.text
        except requests.RequestException as e:
            ultimo_error = e
            time.sleep(2)
    raise ultimo_error


def scrape() -> list[Event]:
    html = _fetch_con_reintentos(URL)
    soup = BeautifulSoup(html, "html.parser")
    eventos = []

    for card in soup.select(".small-box-info"):
        titulo_el = card.select_one(".title")
        fecha_el = card.select_one(".fecha")
        place_el = card.select_one(".place")
        tag_el = card.select_one(".tag")
        link_el = card.select_one("a[href]")
        img_el = card.select_one("img")

        if not titulo_el:
            continue

        eventos.append(Event(
            titulo=titulo_el.get_text(strip=True),
            venue=place_el.get_text(strip=True) if place_el else None,
            ciudad="Rosario",
            fecha_inicio=_parse_fecha(fecha_el.get_text(strip=True) if fecha_el else ""),
            categoria=tag_el.get_text(strip=True) if tag_el else None,
            imagen_url=img_el.get("src") if img_el else None,
            ticket_url=link_el.get("href") if link_el else None,
            fuente=FUENTE,
        ))

    return eventos


if __name__ == "__main__":
    for e in scrape():
        print(e.to_dict())
