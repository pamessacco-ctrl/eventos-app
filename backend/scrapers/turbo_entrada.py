"""Scraper para turboentrada.com.

Es una SPA (Vue) que corre sobre el motor de EntradaUno. Descubrimos via
Playwright que consume `.../v1/api/v2/Cartelera`, una API JSON con toda la
cartelera + venues en un solo call. La llamamos directo con requests.
"""

import requests

from .base import DEFAULT_HEADERS, Event

API_URL = "https://api-ecommerce-live-turboentrada.entradauno.com/v1/api/v2/Cartelera"
WEB_BASE = "https://www.turboentrada.com"
FUENTE = "turboentrada.com"


def scrape() -> list[Event]:
    resp = requests.get(API_URL, headers=DEFAULT_HEADERS, timeout=20)
    resp.raise_for_status()
    data = resp.json()

    cartel = data.get("oData", {}).get("oCartelera", {}) or {}
    establecimientos = {
        e["idEstablecimiento"]: e
        for e in (cartel.get("listaEstablecimiento") or [])
    }

    eventos = []
    for esp in cartel.get("listaEspectaculoCartel") or []:
        funcion = esp.get("oFuncionMenor")
        if not funcion:
            continue  # sin función/fecha asociada (ej. banners de sponsors)

        fecha = (funcion.get("oFuncionFecha") or {}).get("dFuncion")

        venue = None
        ciudad = None
        ids_establecimiento = esp.get("listaIdEstablecimiento") or []
        if ids_establecimiento:
            est = establecimientos.get(ids_establecimiento[0])
            if est:
                venue = est.get("cNombre")
                ciudad = est.get("cZona")

        titulo = (esp.get("cNombre") or "").strip()
        precio = esp.get("fPrecioDesde") or None
        seo = esp.get("cSeo")

        eventos.append(Event(
            titulo=titulo,
            venue=venue,
            ciudad=ciudad,
            fecha_inicio=fecha,
            precio_desde=precio if precio else None,
            imagen_url=esp.get("cImagenBanner"),
            ticket_url=f"{WEB_BASE}/evento/{seo}" if seo else esp.get("cWebUri"),
            fuente=FUENTE,
        ))

    return eventos


if __name__ == "__main__":
    for e in scrape():
        print(e.to_dict())
