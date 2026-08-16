"""Lista curada a mano de artistas/actos internacionales (no argentinos) que
suelen aparecer en la cartelera. Se usa para marcar `Event.artista_internacional`
y armar la sección de "Destacados" de la app con nombres reconocibles de
afuera, agrupando por artista (un mismo artista puede tener shows en varias
provincias/venues y no queremos que ocupe varios lugares del carrusel).

Es un heurístico por nombre, no una base de datos real de artistas: puede
haber falsos negativos (artista internacional que no está en la lista) y,
más raro, falsos positivos si un acto local comparte nombre. Si falta
alguno, se suma acá.

Cada entrada es (nombre_canónico, patrón_regex). El nombre canónico es el
que se usa para agrupar/mostrar; el patrón es lo que se busca en el título
del evento (bordes de palabra, insensible a mayúsculas).

EL ORDEN DE LA LISTA IMPORTA: se usa como ranking de relevancia para elegir
los "Destacados" de la app (los primeros de la lista salen priorizados por
sobre los últimos, sin importar qué tan pronto sea cada show). Es una
priorización manual y subjetiva ("cuán masivo/reconocido es este acto"), no
un dato objetivo — se puede reordenar libremente.
"""

import re

ARTISTAS_INTERNACIONALES = [
    # headliners globales de primer nivel
    ("Ed Sheeran", "Ed Sheeran"),
    ("Iron Maiden", "Iron Maiden"),
    ("Robbie Williams", "Robbie Williams"),
    ("Deep Purple", "Deep Purple"),
    ("Marc Anthony", "Marc Anthony"),
    ("Black Eyed Peas", "Black Eyed Peas"),
    ("David Bisbal", "David Bisbal"),
    ("ZZ Top", "ZZ Top"),
    ("Rush", "Rush"),
    ("Def Leppard", "Def Leppard"),
    ("Slayer", "Slayer"),
    ("Cypress Hill", "Cypress Hill"),
    ("Ozuna", "Ozuna"),
    ("Camilo", r"Camilo(?!\s+Nicolas)"),
    ("ZAYN", "ZAYN"),
    ("Louis Tomlinson", "Louis Tomlinson"),
    ("Die Toten Hosen", "Die Toten Hosen"),
    ("Eros Ramazzotti", "Eros Ramazzotti"),
    ("Helloween", "Helloween"),
    ("Ronnie Wood", "Ronnie Wood"),
    ("Fatboy Slim", "Fatboy Slim"),
    ("Reik", "Reik"),
    ("Sin Bandera", "Sin Bandera"),
    ("Jesse & Joy", r"Jesse (?:&|y) Joy"),
    ("La Oreja de Van Gogh", "La Oreja de Van Gogh"),
    ("Grupo Frontera", "Grupo Frontera"),
    ("Enrique Bunbury", "Enrique Bunbury"),
    ("Sergio Dalma", "Sergio Dalma"),
    ("José Carreras", "Jos[eé] Carreras"),
    ("A Perfect Circle", "A Perfect Circle"),
    ("Puscifer", "Puscifer"),
    ("Jack Johnson", "Jack Johnson"),
    ("Babymetal", "Babymetal"),
    ("Morat", "Morat"),
    ("Cultura Profética", "Cultura Prof[eé]tica"),
    ("Arcángel", "Arc[aá]ngel"),
    ("Kany García", "Kany Garc[ií]a"),
    ("Aitana", "Aitana"),
    ("Myriam Hernández", "Myriam Hern[aá]ndez"),
    ("Alex Ubago", "Alex Ubago"),
    ("Rawayana", "Rawayana"),
    ("Bad Gyal", "Bad Gyal"),
    ("Moscow State Ballet", "Moscow State Ballet"),
    # actos más de nicho (electrónica/DJs, indie, clásica)
    ("Chapterhouse", "Chapterhouse"),
    ("Feine Sahne Fischfilet", "Feine Sahne Fischfilet"),
    ("Giant Rooks", "Giant Rooks"),
    ("James Zabiela", "James Zabiela"),
    ("John Digweed", "John Digweed"),
    ("Guy J", "Guy J"),
    ("PJ Morton", "PJ Morton"),
    ("Cirkus Cirkör", "Cirkus Cirk[oö]r"),
    ("Jakub Józef Orliński", "Jakub J[oó]zef Orli[nń]ski"),
]

_PATRONES = [
    (nombre, re.compile(rf"\b{patron}\b", re.IGNORECASE))
    for nombre, patron in ARTISTAS_INTERNACIONALES
]

_PRIORIDAD = {nombre: idx for idx, (nombre, _) in enumerate(ARTISTAS_INTERNACIONALES)}


def artista_internacional(titulo: str) -> str | None:
    """Devuelve el nombre canónico del artista si el título matchea alguno
    de la lista, o None si no es un acto internacional reconocido."""
    if not titulo:
        return None
    for nombre, patron in _PATRONES:
        if patron.search(titulo):
            return nombre
    return None


def prioridad_destacado(nombre_artista: str | None) -> int | None:
    """Posición del artista en el ranking de relevancia (0 = el más masivo).
    None si no es un artista internacional reconocido."""
    if nombre_artista is None:
        return None
    return _PRIORIDAD.get(nombre_artista)


def es_internacional(titulo: str) -> bool:
    return artista_internacional(titulo) is not None
