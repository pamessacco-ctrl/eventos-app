"""Orquestador: corre todos los scrapers disponibles, normaliza, deduplica
y escribe output/events.json (lo que después consume la app Android)."""

import json
import sys
import traceback
from datetime import datetime, timezone
from pathlib import Path

from scrapers import (
    quality_center, all_access, livepass, mi_anticipada,
    venti, turbo_entrada, movistar_arena,
)
from scrapers.base import dedupe

OUTPUT_PATH = Path(__file__).parent / "output" / "events.json"

SCRAPERS = [
    ("quality_center", quality_center.scrape),
    ("all_access", all_access.scrape),
    ("livepass", livepass.scrape),
    ("mi_anticipada", mi_anticipada.scrape),
    ("venti", venti.scrape),
    ("turbo_entrada", turbo_entrada.scrape),
    ("movistar_arena", movistar_arena.scrape),
]


def run():
    todos = []
    resumen = {}

    for nombre, fn in SCRAPERS:
        try:
            eventos = fn()
            todos.extend(eventos)
            resumen[nombre] = len(eventos)
            print(f"[OK] {nombre}: {len(eventos)} eventos")
        except Exception as e:
            resumen[nombre] = f"ERROR: {e}"
            print(f"[ERROR] {nombre}: {e}")
            traceback.print_exc(file=sys.stderr)

    todos = dedupe(todos)
    todos.sort(key=lambda e: e.fecha_inicio or "9999")

    salida = {
        "generado": datetime.now(timezone.utc).isoformat(),
        "total_eventos": len(todos),
        "resumen_por_fuente": resumen,
        "eventos": [e.to_dict() for e in todos],
    }

    OUTPUT_PATH.parent.mkdir(exist_ok=True)
    OUTPUT_PATH.write_text(json.dumps(salida, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"\nTotal: {len(todos)} eventos -> {OUTPUT_PATH}")


if __name__ == "__main__":
    run()
