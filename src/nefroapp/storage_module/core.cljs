(ns nefroapp.storage-module.core
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
    [nefroapp.storage-module.firebase :as firebase]
    [nefroapp.storage-module.local-storage :as local-storage]
    [nefroapp.util :as util :refer [<sub >evt]]
    [re-frame.core :as re-frame]
    ))

(def initial-state
  {:domain {:pacientes {
    -1 {:id -1, ;; Se começar do zero o firebase vai resgatar o valor como array e não como map.
     :nome "Paciente Exemplo",
     :receitas
     '({:criada-em "2020-05-16T18:11:17.998-03:00[SYSTEM]",
       :editada-em "2020-05-17T10:35:57.080-02:00[SYSTEM]",
       :farmacos
       {"Tempo" {:prescricao "4 HORAS"},
        "Fluxo de Sangue" {:prescricao "400 ml/min"}
        "Fluxo de Dialisato" {:prescricao "500 ml/min"}
        "Acesso Vascular" {:prescricao "Cateter de longa permanência em VJID"}
        "Capilar" {:prescricao "Elisio 21 H"}
        "Peso Seco" {:prescricao "61,5 KG"}
        "KCL" {:prescricao "2,0  meq/L"}
        "Ca" {:prescricao "3 meq/L"}
        "Glicose" {:prescricao "sim"}
        "Na" {:prescricao "138 meq/L"}
        "BIC" {:prescricao "-4"}
        "Temperatura" {:prescricao "36°C"}
        "Heparina" {:prescricao "1,2 ml"}
        "UF Máxima" {:prescricao "3500 ML"}}}
       {:criada-em "2020-04-16T18:11:17.998-03:00[SYSTEM]",
       :editada-em "2020-04-16T18:35:57.080-02:00[SYSTEM]",
       :farmacos
       {"Tempo" {:prescricao "4 HORAS"},
        "Fluxo de Sangue" {:prescricao "400 ml/min"}
        "Fluxo de Dialisato" {:prescricao "500 ml/min"}
        "Acesso Vascular" {:prescricao "Cateter de longa permanência em VJID"}
        "Capilar" {:prescricao "Elisio 21 H"}
        "Peso Seco" {:prescricao "61,5 KG"}
        "KCL" {:prescricao "2,0  meq/L"}
        "Ca" {:prescricao "3 meq/L"}
        "Glicose" {:prescricao "sim"}
        "Na" {:prescricao "138 meq/L"}
        "BIC" {:prescricao "-4"}
        "Temperatura" {:prescricao "36°C"}
        "Heparina" {:prescricao "1,0 ml"}
        "UF Máxima" {:prescricao "3500 ML"}}}
       {:criada-em "2020-03-16T18:11:17.998-03:00[SYSTEM]",
        :editada-em "2020-03-16T18:35:57.080-02:00[SYSTEM]",
        :farmacos
        {"Tempo" {:prescricao "3 HORAS"},
         "Fluxo de Sangue" {:prescricao "400 ml/min"}
         "Fluxo de Dialisato" {:prescricao "500 ml/min"}
         "Acesso Vascular" {:prescricao "Cateter de longa permanência em VJID"}
         "Capilar" {:prescricao "Elisio 21 H"}
         "Peso Seco" {:prescricao "61,5 KG"}
         "KCL" {:prescricao "2,0  meq/L"}
         "Ca" {:prescricao "3 meq/L"}
         "Glicose" {:prescricao "sim"}
         "Na" {:prescricao "138 meq/L"}
         "BIC" {:prescricao "-4"}
         "Temperatura" {:prescricao "36°C"}
         "Heparina" {:prescricao "1,0 ml"}
         "UF Máxima" {:prescricao "3500 ML"}}}
       {:criada-em "2020-03-16T18:11:17.998-03:00[SYSTEM]",
        :editada-em "2020-03-16T18:35:57.080-02:00[SYSTEM]",
        :farmacos
        {"Tempo" {:prescricao "5 HORAS"},
         "Fluxo de Sangue" {:prescricao "400 ml/min"}
         "Fluxo de Dialisato" {:prescricao "500 ml/min"}
         "Acesso Vascular" {:prescricao "Cateter de longa permanência em VJID"}
         "Capilar" {:prescricao "Elisio 21 H"}
         "Peso Seco" {:prescricao "61,5 KG"}
         "KCL" {:prescricao "2,0  meq/L"}
         "Ca" {:prescricao "3 meq/L"}
         "Glicose" {:prescricao "sim"}
         "Na" {:prescricao "138 meq/L"}
         "BIC" {:prescricao "-4"}
         "Temperatura" {:prescricao "36°C"}
         "Heparina" {:prescricao "1,0 ml"}
         "UF Máxima" {:prescricao "3500 ML"}}})}}}
   :ui {:screen-state "pacientes"}})

(re-frame/reg-event-db ::update-domain-or-init
  (fn-traced [app-state [event domain-state]]
    (if domain-state
      (do (local-storage/set-item! "domain" domain-state)
          {:domain domain-state
           :ui {:screen-state "pacientes"}})
      (do (firebase/save! "domain" (:domain initial-state))
          (local-storage/set-item! "domain" (:domain initial-state))
          initial-state))))

;; ------ The save ------

(defn save-or-restore-domain! [{value :domain :as app-state}]
  ;; Only save data if there's already data in the local-storage, otherwise
  ;; restore from the firebase. If both are empty them save data in both
  (if (local-storage/get-item "domain")
    (do (local-storage/set-item! "domain" value)
        (firebase/save! "domain" value))
    (do (js/console.log "Não tinha :domain no Local Storage ao tentar salvar, ignorando save e tentando resgatar do Firebase.")
        (firebase/async-load "domain" #(>evt [::update-domain-or-init %]))
        (js/alert "Seus dados estavam desincronizados com o servidor\nPor favor tente de novo.")))
  app-state)

;; ------ The load ------

;; This is done only when the app is initialized.
(re-frame/reg-event-db
  ::load-domain-from-local-storage
  (fn-traced [app-state]
    ;; Restore from the firebase if there's nothing in the local-storage.
    (if-let [local-domain (local-storage/get-item "domain")]
      {:domain local-domain
       :ui {:screen-state "pacientes"}}
      (do (js/console.log "Sem :domain no Local Storage, tentando no Firebase agora..")
          (firebase/async-load "domain" #(>evt [::update-domain-or-init %]))
          {:ui {:screen-state "loading"}}))))

(defn register-firebase-load! []
  (firebase/register-load! "domain"
    (fn [snap-val]
      (when (not= snap-val (local-storage/get-item "domain"))
        (>evt [::update-domain-or-init snap-val])))))
