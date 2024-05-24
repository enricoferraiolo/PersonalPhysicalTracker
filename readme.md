- db con room
  - 3 cosi per ora, activities sono quelle registrate da utente.
  - activitieslist sono le attivita in generale, quelle nell'app / aggiunte da utente
- stopwatch service
  - serve per interagire con lo stopwatch in background
  - si occupa anche di mandare notifiche foreground
  - si binda con mainactivity, dopodiche homefragment manda richieste tramite interfaccia stopwatchcontrollistener alla mainactivity, questa attraverso si connette al mio service manda richieste per conto del fragment richiedente
    - inoltre homefragment e mainactivity usando anche un SharedTimeViewModel per scambiarsi informazioni sullo stopwatch 

  