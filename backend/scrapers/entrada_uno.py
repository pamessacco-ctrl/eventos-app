"""Scraper para entradauno.com (el portal, no sus clientes).

El sitio en sí está roto ahora mismo: absolutamente todos sus assets .js
devuelven el HTML del shell en vez de JavaScript real (probamos con
requests puro, sin browser: mismo resultado), así que el front-end no
carga para nadie, bot o humano. Pero como es el mismo motor que
turboentrada.com, probamos el mismo patrón de API y funciona:

    https://api-ecommerce-live-entradauno.entradauno.com/v1/api/v2/Cartelera

Este endpoint no es solo de "entradauno" la marca: es el catálogo agregado
de TODO lo que se vende con el motor EntradaUno (incluye turboentrada.com,
qualitycenter.com y otros sitios blanco). Trae ~400 eventos de una sola
llamada. Puede solapar con turbo_entrada.py y quality_center.py — el
dedupe() de main.py ya colapsa duplicados exactos (mismo título+fecha+venue).
"""

import requests

from .base import DEFAULT_HEADERS, Event

API_URL = "https://api-ecommerce-live-entradauno.entradauno.com/v1/api/v2/Cartelera"
FUENTE = "entradauno.com"


def scrape() -> list[Event]:
    resp = requests.get(API_URL, headers=DEFAULT_HEADERS, timeout=30)
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
            continue  # sin función/fecha asociada (banners, sponsors, etc.)

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

        eventos.append(Event(
            titulo=titulo,
            venue=venue,
            ciudad=ciudad,
            fecha_inicio=fecha,
            precio_desde=precio if precio else None,
            imagen_url=esp.get("cImagenBanner"),
            ticket_url=esp.get("cWebUri"),
            fuente=FUENTE,
        ))

    return eventos


if __name__ == "__main__":
    for e in scrape():
        print(e.to_dict())
