"""Scraper para mianticipada.com.

No hace falta parsear HTML: el sitio expone un endpoint JSON interno
(usado por su propio buscador) que devuelve los eventos ya estructurados.
"""

import re
from datetime import datetime

import requests

from .base import DEFAULT_HEADERS, Event

URL = "https://mianticipada.com/obtener_eventos/"
BASE = "https://mianticipada.com"
FUENTE = "mianticipada.com"


def _parse_fecha(desde_fecha: str) -> str | None:
    # formato: "16/08/2026, 20:00"
    if not desde_fecha:
        return None
    try:
        dt = datetime.strptime(desde_fecha.strip(), "%d/%m/%Y, %H:%M")
        return dt.isoformat()
    except ValueError:
        return None


def _split_lugar(lugar: str) -> tuple[str | None, str | None]:
    # formato típico: "El Padilla, Temperley, Buenos Aires" -> venue, ciudad
    # (nos quedamos solo con "Temperley", no con "Temperley, Buenos Aires",
    # para que el filtro de localidad de la app no quede lleno de variantes
    # compuestas con la provincia repetida en casi todas)
    if not lugar:
        return None, None
    partes = [p.strip() for p in lugar.split(",") if p.strip()]
    if len(partes) >= 2:
        return partes[0], partes[1]
    return partes[0] if partes else None, None


def _limpiar_titulo(nombre: str) -> str:
    # nombre viene como "2026-08-16 | FLORES EN LA LUNA En Temperley"
    return re.sub(r"^\d{4}-\d{2}-\d{2}\s*\|\s*", "", nombre or "").strip()


def scrape() -> list[Event]:
    resp = requests.get(
        URL, params={"cantidad_eventos": 500}, headers=DEFAULT_HEADERS, timeout=20
    )
    resp.raise_for_status()
    data = resp.json()

    eventos = []
    for ev in data.get("eventos", []):
        titulo = _limpiar_titulo(ev.get("nombre", ""))
        if not titulo:
            continue

        venue, ciudad = _split_lugar(ev.get("lugar", ""))

        precio = None
        precio_txt = (ev.get("entradas_online_desde") or "").strip()
        if precio_txt:
            try:
                precio = float(precio_txt)
            except ValueError:
                precio = None

        imagen = ev.get("imagen_home")
        if imagen and imagen.startswith("/"):
            imagen = BASE + imagen

        eventos.append(Event(
            titulo=titulo,
            venue=venue,
            ciudad=ciudad,
            fecha_inicio=_parse_fecha(ev.get("desde_fecha", "")),
            precio_desde=precio,
            moneda=ev.get("moneda") or "ARS",
            imagen_url=imagen,
            ticket_url=ev.get("url_externa"),
            fuente=FUENTE,
        ))

    return eventos


if __name__ == "__main__":
    for e in scrape():
        print(e.to_dict())
