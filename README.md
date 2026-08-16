# Eventos App

App Android personal que unifica en un solo calendario los eventos de varios
sitios de ticketing argentinos.

## Estructura

- `backend/` — scrapers en Python. Cada sitio tiene su módulo en
  `backend/scrapers/`, normalizado a un esquema común (`scrapers/base.py`).
  `backend/main.py` corre todos los scrapers y genera `backend/output/events.json`.
- `.github/workflows/scrape.yml` — corre el backend cada 6 horas en GitHub
  Actions y commitea el `events.json` actualizado al repo.
- `android/` — app Android nativa (Kotlin + Jetpack Compose) que consume ese JSON: calendario mensual, buscador, detalle de evento con "Comprar" y "Agregar a mi calendario".

## Backend: correr localmente

```bash
cd backend
pip install -r requirements.txt
python -m playwright install chromium   # solo necesario una vez
python main.py
```

Genera `backend/output/events.json`.

## Fuentes cubiertas (9)

| Sitio | Método |
|---|---|
| Quality Center | HTML server-rendered |
| All Access | HTML server-rendered (falla desde IPs de datacenter, ver nota abajo) |
| LivePass | HTML server-rendered |
| Mi Anticipada | endpoint JSON interno |
| Venti | API JSON pública |
| TurboEntrada | API JSON del motor EntradaUno |
| Movistar Arena | Playwright + JSON-LD schema.org |
| entradauno.com | API JSON del motor EntradaUno (catálogo agregado, ~400 eventos de golpe) |
| Rosario en Cartel | HTML server-rendered (hosting lento, tiene reintentos) |

**Sin resolver:** Passline — tiene un challenge de Cloudflare que bloquea
navegadores automatizados. Decisión consciente: no se intentó bypass, porque
el sitio está señalizando explícitamente que no quiere tráfico de bots.

**Nota sobre All Access:** el sitio sirve vía AWS CloudFront, que bloquea
IPs de datacenter conocidas (incluidas las de GitHub Actions) con un 403 —
pero anda perfecto desde una IP residencial normal. El scraping en la nube
sigue trayendo las otras 7 fuentes igual (el orquestador aísla errores por
scraper). Si corrés `python main.py` desde tu PC de casa, ese sí se captura.

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
