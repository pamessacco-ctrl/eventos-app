"""Scraper para venti.live.

El sitio es una SPA (React), pero descubrimos via Playwright que consume una
API JSON propia (api.venti.live) sin protección ni CORS server-side, así que
la llamamos directo con requests: mucho más rápido y estable que un browser.
"""

import requests

from .base import DEFAULT_HEADERS, Event

API_URL = "https://api.venti.live/api/home/events"
FUENTE = "venti.live"
PAGE_SIZE = 100


def scrape() -> list[Event]:
    eventos = []
    page = 1
    while True:
        resp = requests.get(
            API_URL, params={"limit": PAGE_SIZE, "page": page},
            headers=DEFAULT_HEADERS, timeout=20,
        )
        resp.raise_for_status()
        data = resp.json()

        for ev in data.get("events", []):
            venue_info = ev.get("venue") or {}
            venue = venue_info.get("placeName") or ev.get("placeName") or None
            ciudad = (venue_info.get("city") or {}).get("name")

            eventos.append(Event(
                titulo=(ev.get("name") or "").strip(),
                venue=venue,
                ciudad=ciudad,
                fecha_inicio=ev.get("startDate"),
                imagen_url=ev.get("bannerImg"),
                ticket_url=f"https://venti.live/evento/{ev['urlName']}" if ev.get("urlName") else None,
                fuente=FUENTE,
            ))

        total_pages = data.get("totalPages", 1)
        if page >= total_pages:
            break
        page += 1

    return eventos


if __name__ == "__main__":
    for e in scrape():
        print(e.to_dict())
