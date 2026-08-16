"""Lista curada a mano de artistas/actos internacionales (no argentinos) que
suelen aparecer en la cartelera. Se usa para marcar `Event.es_internacional`
y armar la sección de "Destacados" de la app con nombres reconocibles de
afuera, en vez de simplemente "lo más próximo en fecha".

Es un heurístico por nombre, no una base de datos real de artistas: puede
haber falsos negativos (artista internacional que no está en la lista) y,
más raro, falsos positivos si un acto local comparte nombre. Si falta
alguno, se suma acá.
"""

import re

ARTISTAS_INTERNACIONALES = [
    # rock / pop internacional
    "Robbie Williams", "Black Eyed Peas", "ZAYN", "Louis Tomlinson",
    "Ed Sheeran", "Deep Purple", "Def Leppard", "Die Toten Hosen",
    "Iron Maiden", "Rush", "Slayer", "ZZ Top", "A Perfect Circle",
    "Puscifer", "Cypress Hill", "Fatboy Slim", "Jack Johnson",
    "Chapterhouse", "Feine Sahne Fischfilet", "Giant Rooks", "Babymetal",
    "Helloween", "Eros Ramazzotti", "Ronnie Wood",
    # latino / reggaetón / pop en español (de afuera de Argentina)
    "Ozuna", r"Camilo(?!\s+Nicolas)", "Marc Anthony", "Reik", "Jesse (?:&|y) Joy",
    "Sin Bandera", "Grupo Frontera", "Bad Gyal", "Morat",
    "Cultura Prof[eé]tica", "Rawayana", "Arc[aá]ngel", "Kany Garc[ií]a",
    "Myriam Hern[aá]ndez",
    # españoles
    "David Bisbal", "Jos[eé] Carreras", "Alex Ubago", "Aitana",
    "Enrique Bunbury", "Sergio Dalma", "La Oreja de Van Gogh",
    # electrónica / DJs internacionales
    "James Zabiela", "John Digweed", "Guy J", "PJ Morton",
    # otros / danza / clásica
    "Moscow State Ballet", "Cirkus Cirk[oö]r", "Jakub J[oó]zef Orli[nń]ski",
]

# compilamos como regex con bordes de palabra, insensible a mayúsculas
_PATRONES = [re.compile(rf"\b{p}\b", re.IGNORECASE) for p in ARTISTAS_INTERNACIONALES]


def es_internacional(titulo: str) -> bool:
    if not titulo:
        return False
    return any(p.search(titulo) for p in _PATRONES)
