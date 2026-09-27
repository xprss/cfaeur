# CFA ⇄ EUR

Convertitore Android offline tra franco CFA dell'Africa occidentale (FCFA/XOF) ed euro. Include un widget 4×4 espandibile a 4×5 o 5×4 con tastierino sulla schermata Home e una schermata di conversione nell'app.

Il calcolo usa la parità ufficiale [BCEAO](https://www.bceao.int/fr/content/histoire-du-franc-cfa): **1 EUR = 655,957 FCFA**. Gli importi in EUR sono arrotondati a due decimali; quelli in FCFA all'unità. Eventuali commissioni di cambio non sono incluse.

## Sviluppo

Apri il progetto in Android Studio oppure usa `./gradlew testDebugUnitTest assembleDebug`. Servono JDK 17 e Android SDK 35 con Build Tools 35.0.0. L'APK di debug si trova in `app/build/outputs/apk/debug/`.

Il tasto `⇄` scambia le valute e usa il risultato arrotondato come nuovo importo. `C` azzera l'importo; `⌫` elimina l'ultima cifra. La virgola è disponibile quando l'importo di partenza è in EUR. Ogni istanza del widget conserva separatamente il proprio stato.

## Release su GitHub

Il workflow `.github/workflows/android.yml` verifica ogni push e pull request. Un tag `vX.Y.Z`, per esempio `v1.0.0`, compila un APK firmato e crea una GitHub Release che contiene l'APK e `SHA256SUMS.txt`.

Configura nel repository questi **GitHub Actions Secrets**, usando la tua chiave Android esistente:

| Secret | Contenuto |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Keystore codificato in Base64, tutto su una riga |
| `ANDROID_KEYSTORE_PASSWORD` | Password del keystore |
| `ANDROID_KEY_ALIAS` | Alias della chiave |
| `ANDROID_KEY_PASSWORD` | Password della chiave |

Su Linux puoi ottenere il valore Base64 con `base64 -w 0 /percorso/alla/chiave.jks` e incollarlo nel Secret. Conservala anche in un backup privato: per aggiornare l'app installata servirà la stessa chiave. Non inserire il keystore o le password nel repository. Non sono necessarie GitHub Actions Variables; il workflow usa il `GITHUB_TOKEN` automatico e richiede il permesso `contents: write` per creare la Release.

Dopo aver configurato i Secrets e pubblicato il codice, crea e invia un tag `vX.Y.Z`. La pagina **Releases** del repository conterrà l'APK scaricabile. Per le versioni successive usa tag crescenti.
