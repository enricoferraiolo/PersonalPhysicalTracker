- readme
- db con room
  - 3 cosi per ora, activities sono quelle registrate da utente.
  - activitieslist sono le attivita in generale, quelle nell'app / aggiunte da utente
- stopwatch service
  - serve per interagire con lo stopwatch in background
  - si occupa anche di mandare notifiche foreground
  - si binda con mainactivity, dopodiche homefragment manda richieste tramite interfaccia stopwatchcontrollistener alla mainactivity, questa attraverso si connette al mio service manda richieste per conto del fragment richiedente
    - inoltre homefragment e mainactivity usando anche un SharedTimeViewModel per scambiarsi informazioni sullo stopwatch 
  - si può mettere in pausa perche cosi l'utente può controllare e modificare l'attività in corso
- Calendar
  - un'attività registrata viene visualizzata in un dato giorno sse il timestamp della data:
    - di inizio attività è il giorno corrente-1 e la data di fine attività è dentro al giorno corrente
    - di fine attività è il giorno corrente+1 e la data di inizio attività è dentro al giorno corrente
    - e se un'attività dura più di un giorno?
      - da vedere come funziona in primis
        - posso decidere che viene visualizzata dove inizia e dove finisce e basta
  - un'activity registrata con foreign key eliminata (null) viene vista come "Uknown activity"