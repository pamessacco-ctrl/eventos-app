# Eventos App

App Android personal que unifica en un solo calendario los eventos de varios
sitios de ticketing argentinos.

## Estructura

- `backend/` — scrapers en Python. Cada sitio tiene su módulo en
  `backend/scrapers/`, normalizado a un esquema común (`scrapers/base.py`).
  `backend/main.py` corre todos los scrapers y genera `backend/output/events.json`.
- `.github/workflows/scrape.yml` — corre el backend cada 6 horas en GitHub
  Actions y commitea el `events.json` actualizado al repo.
- `android/` — (todavía no existe) la app Kotlin/Compose que consume ese JSON.

## Backend: correr localmente

```bash
cd backend
pip install -r requirements.txt
python -m playwright install chromium   # solo necesario una vez
python main.py
```

Genera `backend/output/events.json`.

## Fuentes cubiertas (7 de 9)

| Sitio | Método |
|---|---|
| Quality Center | HTML server-rendered |
| All Access | HTML server-rendered |
| LivePass | HTML server-rendered |
| Mi Anticipada | endpoint JSON interno |
| Venti | API JSON pública |
| TurboEntrada | API JSON del motor EntradaUno |
| Movistar Arena | Playwright + JSON-LD schema.org |

**Sin resolver:** Passline (bloqueado por Cloudflare — no se intentó bypass a
propósito) y entradauno.com (portal del proveedor, SPA que no termina de
cargar los datos).

## Poner esto en GitHub (para que el scraping corra solo)

1. Creá un repo en GitHub (puede ser público — el `events.json` son eventos
   públicos, no hay datos sensibles).
2. Conectá este repo local:
   ```bash
   git remote add origin https://github.com/<tu-usuario>/<tu-repo>.git
   git branch -M main
   git push -u origin main
   ```
3. En GitHub, andá a la pestaña **Actions** del repo y activá los workflows
   si te lo pide. El cron ya corre cada 6 horas solo; también podés
   dispararlo a mano desde ahí ("Run workflow").
4. La app Android va a leer el JSON actualizado desde:
   `https://raw.githubusercontent.com/<tu-usuario>/<tu-repo>/main/backend/output/events.json`
