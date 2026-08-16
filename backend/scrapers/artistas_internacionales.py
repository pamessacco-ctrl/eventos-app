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
"""

import re

ARTISTAS_INTERNACIONALES = [
    # rock / pop internacional
    ("Robbie Williams", "Robbie Williams"),
    ("Black Eyed Peas", "Black Eyed Peas"),
    ("ZAYN", "ZAYN"),
    ("Louis Tomlinson", "Louis Tomlinson"),
    ("Ed Sheeran", "Ed Sheeran"),
    ("Deep Purple", "Deep Purple"),
    ("Def Leppard", "Def Leppard"),
    ("Die Toten Hosen", "Die Toten Hosen"),
    ("Iron Maiden", "Iron Maiden"),
    ("Rush", "Rush"),
    ("Slayer", "Slayer"),
    ("ZZ Top", "ZZ Top"),
    ("A Perfect Circle", "A Perfect Circle"),
    ("Puscifer", "Puscifer"),
    ("Cypress Hill", "Cypress Hill"),
    ("Fatboy Slim", "Fatboy Slim"),
    ("Jack Johnson", "Jack Johnson"),
    ("Chapterhouse", "Chapterhouse"),
    ("Feine Sahne Fischfilet", "Feine Sahne Fischfilet"),
    ("Giant Rooks", "Giant Rooks"),
    ("Babymetal", "Babymetal"),
    ("Helloween", "Helloween"),
    ("Eros Ramazzotti", "Eros Ramazzotti"),
    ("Ronnie Wood", "Ronnie Wood"),
    # latino / reggaetón / pop en español (de afuera de Argentina)
    ("Ozuna", "Ozuna"),
    ("Camilo", r"Camilo(?!\s+Nicolas)"),
    ("Marc Anthony", "Marc Anthony"),
    ("Reik", "Reik"),
    ("Jesse & Joy", r"Jesse (?:&|y) Joy"),
    ("Sin Bandera", "Sin Bandera"),
    ("Grupo Frontera", "Grupo Frontera"),
    ("Bad Gyal", "Bad Gyal"),
    ("Morat", "Morat"),
    ("Cultura Profética", "Cultura Prof[eé]tica"),
    ("Rawayana", "Rawayana"),
    ("Arcángel", "Arc[aá]ngel"),
    ("Kany García", "Kany Garc[ií]a"),
    ("Myriam Hernández", "Myriam Hern[aá]ndez"),
    # españoles
    ("David Bisbal", "David Bisbal"),
    ("José Carreras", "Jos[eé] Carreras"),
    ("Alex Ubago", "Alex Ubago"),
    ("Aitana", "Aitana"),
    ("Enrique Bunbury", "Enrique Bunbury"),
    ("Sergio Dalma", "Sergio Dalma"),
    ("La Oreja de Van Gogh", "La Oreja de Van Gogh"),
    # electrónica / DJs internacionales
    ("James Zabiela", "James Zabiela"),
    ("John Digweed", "John Digweed"),
    ("Guy J", "Guy J"),
    ("PJ Morton", "PJ Morton"),
    # otros / danza / clásica
    ("Moscow State Ballet", "Moscow State Ballet"),
    ("Cirkus Cirkör", "Cirkus Cirk[oö]r"),
    ("Jakub Józef Orliński", "Jakub J[oó]zef Orli[nń]ski"),
]

_PATRONES = [
    (nombre, re.compile(rf"\b{patron}\b", re.IGNORECASE))
    for nombre, patron in ARTISTAS_INTERNACIONALES
]


def artista_internacional(titulo: str) -> str | None:
    """Devuelve el nombre canónico del artista si el título matchea alguno
    de la lista, o None si no es un acto internacional reconocido."""
    if not titulo:
        return None
    for nombre, patron in _PATRONES:
        if patron.search(titulo):
            return nombre
    return None


def es_internacional(titulo: str) -> bool:
    return artista_internacional(titulo) is not None
