"""Utilidades compartidas por todos los scrapers: modelo de evento normalizado,
parseo de fechas en español y helpers de HTTP."""

from __future__ import annotations

import hashlib
import re
from dataclasses import dataclass, field, asdict
from datetime import datetime, date
from typing import Optional

import requests

USER_AGENT = (
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
    "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
)

DEFAULT_HEADERS = {
    "User-Agent": USER_AGENT,
    "Accept-Language": "es-AR,es;q=0.9,en;q=0.8",
}

MESES = {
    "enero": 1, "febrero": 2, "marzo": 3, "abril": 4, "mayo": 5, "junio": 6,
    "julio": 7, "agosto": 8, "septiembre": 9, "setiembre": 9, "octubre": 10,
    "noviembre": 11, "diciembre": 12,
}

MESES_ABREV = {
    "ene": 1, "feb": 2, "mar": 3, "abr": 4, "may": 5, "jun": 6,
    "jul": 7, "ago": 8, "sep": 9, "set": 9, "oct": 10, "nov": 11, "dic": 12,
}


@dataclass
class Event:
    titulo: str
    venue: Optional[str] = None
    ciudad: Optional[str] = None
    provincia: Optional[str] = None
    fecha_inicio: Optional[str] = None  # ISO 8601, ej "2026-09-12T21:00:00"
    fecha_fin: Optional[str] = None
    categoria: Optional[str] = None
    precio_desde: Optional[float] = None
    moneda: str = "ARS"
    imagen_url: Optional[str] = None
    ticket_url: Optional[str] = None
    fuente: str = ""
    id: str = field(default="")

    def __post_init__(self):
        if not self.id:
            base = f"{self.fuente}|{self.titulo}|{self.fecha_inicio or ''}"
            self.id = hashlib.sha1(base.encode("utf-8")).hexdigest()[:16]
        self.ciudad = normalizar_ciudad(self.ciudad) if (self.ciudad or "").strip() else None
        if not self.provincia and self.ciudad:
            self.provincia = PROVINCIA_POR_CIUDAD.get(_sin_acentos(self.ciudad).lower())

    def to_dict(self):
        return asdict(self)


# Alias de ciudades: distintos sitios llaman distinto al mismo lugar
# (may/minúsculas, "CABA" vs el nombre completo, "Provincia de X" vs "X").
# Todo en minúscula sin acentos como clave, para matchear sin importar cómo
# venga escrito.
_ALIAS_CIUDAD = {
    "caba": "CABA",
    "ciudad autonoma de buenos aires": "CABA",
    "capital federal": "CABA",
    "provincia de cordoba": "Córdoba",
    "provincia de mendoza": "Mendoza",
    "provincia de santa fe": "Santa Fe",
    "provincia de buenos aires": "Buenos Aires",
}


def _sin_acentos(texto: str) -> str:
    reemplazos = str.maketrans("áéíóúñÁÉÍÓÚÑ", "aeiounAEIOUN")
    return texto.translate(reemplazos)


def normalizar_ciudad(ciudad: str) -> str:
    limpio = ciudad.strip()
    clave = _sin_acentos(limpio).lower()
    if clave in _ALIAS_CIUDAD:
        return _ALIAS_CIUDAD[clave]
    # normaliza mayúsculas/minúsculas sueltas (ej. "CÓRDOBA" -> "Córdoba")
    # pero respeta nombres ya bien capitalizados con mayúsculas internas
    # (ej. "San Nicolás de Los Arroyos") en vez de aplastarlos.
    if limpio.isupper() or limpio.islower():
        return limpio.title()
    return limpio


# Mapeo ciudad/localidad (ya normalizada) -> provincia argentina, para el
# filtro de la app. Clave sin tildes y en minúscula. Lugares que no
# reconocemos quedan con provincia=None (ej. "Costa Argentina", "Montevideo"
# que es Uruguay) y simplemente no aparecen si se filtra por provincia.
_PROVINCIA_POR_CIUDAD_RAW = {
    "CABA": "CABA",
    "Buenos Aires": "Buenos Aires",
    "Bahía Blanca": "Buenos Aires",
    "Berisso": "Buenos Aires",
    "Canning": "Buenos Aires",
    "Carmen de Patagones": "Buenos Aires",
    "General Rodríguez": "Buenos Aires",
    "La Plata": "Buenos Aires",
    "Lanus": "Buenos Aires",
    "Lomas de Zamora": "Buenos Aires",
    "Martinez": "Buenos Aires",
    "Monte Grande": "Buenos Aires",
    "Ramos Mejía": "Buenos Aires",
    "San Isidro": "Buenos Aires",
    "San Justo": "Buenos Aires",
    "San Nicolás de Los Arroyos": "Buenos Aires",
    "Temperley": "Buenos Aires",
    "Tigre": "Buenos Aires",
    "Tornquist": "Buenos Aires",
    "Catamarca": "Catamarca",
    "Chubut": "Chubut",
    "Esquel": "Chubut",
    "Pto. Madryn": "Chubut",
    "Trelew": "Chubut",
    "Córdoba": "Córdoba",
    "Corrientes": "Corrientes",
    "Entre Ríos": "Entre Ríos",
    "Mendoza": "Mendoza",
    "Neuquén": "Neuquén",
    "Plottier": "Neuquén",
    "Cipolletti": "Río Negro",
    "General Roca": "Río Negro",
    "Villa Regina": "Río Negro",
    "Salta": "Salta",
    "Ibarlucea": "Santa Fe",
    "Rosario": "Santa Fe",
    "Santa Fe": "Santa Fe",
    "Santiago del Estero": "Santiago del Estero",
    "Río Grande": "Tierra del Fuego",
    "Ushuaia": "Tierra del Fuego",
    "Tucumán": "Tucumán",
}
PROVINCIA_POR_CIUDAD = {
    _sin_acentos(k).lower(): v for k, v in _PROVINCIA_POR_CIUDAD_RAW.items()
}


def fetch_html(url: str, timeout: int = 20) -> str:
    resp = requests.get(url, headers=DEFAULT_HEADERS, timeout=timeout)
    resp.raise_for_status()
    return resp.text


def parse_fecha_larga(texto: str, anio_default: Optional[int] = None) -> Optional[str]:
    """Parsea fechas tipo '29 de agosto, 2026' o '15 octubre 2026' -> ISO date string."""
    if not texto:
        return None
    texto = texto.strip().lower()
    m = re.search(r"(\d{1,2})\s*(?:de\s*)?([a-záéíóú]+)[,]?\s*(\d{4})?", texto)
    if not m:
        return None
    dia, mes_txt, anio_txt = m.groups()
    mes = MESES.get(mes_txt)
    if not mes:
        return None
    anio = int(anio_txt) if anio_txt else (anio_default or _infer_year(mes, int(dia)))
    try:
        return date(anio, mes, int(dia)).isoformat()
    except ValueError:
        return None


def parse_fecha_abreviada(texto: str, hoy: Optional[date] = None) -> Optional[str]:
    """Parsea fechas tipo '05 SEP' o '20 OCT al 21 OCT' (usa la primera) -> ISO date."""
    if not texto:
        return None
    m = re.search(r"(\d{1,2})\s+([A-Za-záéíóú]{3,})", texto.strip())
    if not m:
        return None
    dia, mes_txt = m.groups()
    mes = MESES_ABREV.get(mes_txt.lower()[:3])
    if not mes:
        return None
    anio = _infer_year(mes, int(dia), hoy)
    try:
        return date(anio, mes, int(dia)).isoformat()
    except ValueError:
        return None


def _infer_year(mes: int, dia: int, hoy: Optional[date] = None) -> int:
    """Si el sitio no da el año, asumimos el próximo que caiga esa fecha (nunca en el pasado)."""
    hoy = hoy or date.today()
    anio = hoy.year
    try:
        candidata = date(anio, mes, dia)
    except ValueError:
        candidata = date(anio, mes, 1)
    if candidata < hoy:
        anio += 1
    return anio


def normalizar_ciudades_global(events: list[Event]) -> None:
    """Segunda pasada, con TODOS los eventos ya juntos: agrupa ciudades que
    solo difieren en tildes (ej. "Bahia Blanca" / "Bahía Blanca", venidas de
    distintas fuentes) y las deja todas escritas igual, prefiriendo la
    variante con tildes si existe. Modifica los eventos in-place."""
    grupos: dict[str, list[str]] = {}
    for ev in events:
        if ev.ciudad:
            grupos.setdefault(_sin_acentos(ev.ciudad).lower(), []).append(ev.ciudad)

    canonico = {}
    for clave, valores in grupos.items():
        con_acentos = [v for v in valores if v != _sin_acentos(v)]
        candidatos = con_acentos or valores
        canonico[clave] = max(candidatos, key=candidatos.count)

    for ev in events:
        if ev.ciudad:
            ev.ciudad = canonico[_sin_acentos(ev.ciudad).lower()]


def dedupe(events: list[Event]) -> list[Event]:
    seen = set()
    result = []
    for ev in events:
        key = (ev.titulo.strip().lower(), (ev.fecha_inicio or "")[:10], (ev.venue or "").strip().lower())
        if key in seen:
            continue
        seen.add(key)
        result.append(ev)
    return result
