(ns ^:figwheel-hooks nefroapp.main
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
    [nefroapp.telas.routing :as routing]
    [re-frame.core :as re-frame]
    [reagent.dom]

    ;; [day8.re-frame-10x]
    ))

(when ^boolean js/goog.DEBUG ;; Code removed in production
  (js/console.log "Debugger mode!"))

(defn register-service-worker! []
  (.addEventListener js/window "load" #(some-> js/navigator
                                               .-serviceWorker
                                               (.register "/nefroapp/sw.js"))))

(def initial-state
  {:domain {:pacientes [
    {:id 0,                                                                       
     :nome "João Alves",                                                              
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
        "UF Máxima" {:prescricao "3500 ML"}
        }}
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
        "UF Máxima" {:prescricao "3500 ML"}
        }}
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
         "UF Máxima" {:prescricao "3500 ML"}
         }}
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
         "UF Máxima" {:prescricao "3500 ML"}
         }}
       )
     }
    {:id 1
     :nome "Raul Araujo",
     :receitas
     '({:criada-em "2020-01-31T13:26:52.291-03:00[SYSTEM]",                            
       :editada-em "2019-12-22T22:55:00.815-03:00[SYSTEM]",                           
       :farmacos {}}
      {:criada-em "2018-10-03T08:24:38.637-03:00[SYSTEM]",                            
       :editada-em "2018-12-18T15:42:04.440-02:00[SYSTEM]",                           
       :farmacos {}}
      {:criada-em "2018-06-20T17:40:57.003-03:00[SYSTEM]",                            
       :editada-em "2018-09-16T14:50:15.273-03:00[SYSTEM]",                           
       :farmacos {}}
      {:criada-em "2018-07-17T11:54:36.422-03:00[SYSTEM]",                            
       :editada-em "2019-12-19T21:07:19.891-03:00[SYSTEM]",                           
       :farmacos {}}
      {:criada-em "2018-11-08T12:54:32.455-02:00[SYSTEM]",                            
       :editada-em "2019-08-21T22:44:22.386-03:00[SYSTEM]",                           
       :farmacos {}}
      {:criada-em "2018-05-05T09:13:19.645-03:00[SYSTEM]",                            
       :editada-em "2019-03-22T12:37:22.225-03:00[SYSTEM]",                           
       :farmacos {}}
      {:criada-em "2019-06-17T18:08:01.838-03:00[SYSTEM]",                            
       :editada-em "2019-05-18T20:47:43.601-03:00[SYSTEM]",                           
       :farmacos {}}
      {:criada-em "2020-01-06T21:16:59.148-03:00[SYSTEM]",                            
       :editada-em "2020-03-12T19:42:26.849-03:00[SYSTEM]",                           
       :farmacos {}})}
    {:id 2
     :nome "Abel Tavares"
     :receitas
     '({:criada-em "2018-09-06T11:24:25.238-03:00[SYSTEM]",                            
        :editada-em "2018-12-02T08:36:33.708-02:00[SYSTEM]",                           
        :farmacos { }})}
    {:id 3
     :nome "Aline Ribeiro",
     :receitas
     '({:criada-em "2019-03-18T13:37:09.041-03:00[SYSTEM]",
       :editada-em "2019-01-31T13:12:09.963-02:00[SYSTEM]",
       :farmacos {}}
      {:criada-em "2019-06-13T06:16:31.030-03:00[SYSTEM]",
       :editada-em "2018-05-18T09:42:35.783-03:00[SYSTEM]", 
       :farmacos {}})}
    {:id 4
     :nome "Anderson Siqueira",
     :receitas
     '({:criada-em "2018-05-29T16:35:59.455-03:00[SYSTEM]",
       :editada-em "2018-10-23T22:26:13.694-03:00[SYSTEM]",
       :farmacos {}}
      {:criada-em "2019-01-02T06:43:06.376-02:00[SYSTEM]",
       :editada-em "2019-09-07T20:19:51.938-03:00[SYSTEM]", 
       :farmacos {}})}
    {:id 5
     :nome "Fernanda Ramos",
     :receitas
     '({:criada-em "2020-03-26T19:53:27.654-03:00[SYSTEM]",
       :editada-em "2018-05-01T11:00:52.583-03:00[SYSTEM]",
       :farmacos {}}
      {:criada-em "2020-03-15T10:19:09.749-03:00[SYSTEM]",
       :editada-em "2018-07-09T11:31:46.411-03:00[SYSTEM]", 
       :farmacos {}})}
    {:id 6
     :nome "Sandra Castro",
     :receitas
     '({:criada-em "2019-05-02T13:18:45.373-03:00[SYSTEM]",                            
       :editada-em "2019-11-26T20:28:58.241-03:00[SYSTEM]",                           
       :farmacos {}}
      {:criada-em "2018-05-31T06:57:23.149-03:00[SYSTEM]",                            
       :editada-em "2019-11-07T22:15:22.997-03:00[SYSTEM]",                           
       :farmacos {}})}
    {:id 7
     :nome "Rodrigo Pereira",
     :receitas
     '({:criada-em "2019-02-16T15:08:51.612-02:00[SYSTEM]",
       :editada-em "2018-10-31T10:29:28.834-03:00[SYSTEM]",
       :farmacos {}}
      {:criada-em "2018-12-19T17:01:53.583-02:00[SYSTEM]",
       :editada-em "2019-03-26T18:53:37.619-03:00[SYSTEM]", 
       :farmacos {}})}]}
   :ui {:screen-state "pacientes"}})

(def initial-state-old
  {:domain
   {:pacientes
    [{:id 0
      :nome "João Paulo Soares"
      :receitas '({:criada-em "2020-04-19T11:07:38.106-03:00[SYSTEM]"
                   :editada-em "2020-04-13T11:07:38.106-03:00[SYSTEM]"
                   :farmacos {"Hidróxido de Ferro" {:prescricao "100ml"}}}
                  {:criada-em "2020-02-14T11:07:38.106-03:00[SYSTEM]"
                   :editada-em "2020-04-13T11:07:38.106-03:00[SYSTEM]"
                   :farmacos {"Hidróxido de Ferro" {:prescricao "2"}}}
                  )}]}
   :ui {:screen-state "receita"}})

(re-frame/reg-event-db ::set-app-state
  (fn-traced [_ [event application-state]]
             application-state))

(defn init-state! []
  (re-frame/dispatch-sync [::set-app-state initial-state]))

(defn ^:after-load mount-app-element! []
  (when ^boolean js/goog.DEBUG ;; Code removed in production
    ;; (day8.re-frame-10x/show-panel! false)
    (re-frame/clear-subscription-cache!))
  (when-let [el (.getElementById js/document "app")]
    (reagent.dom/render [routing/selected-view] el)))

(defonce startup
  (do
    (register-service-worker!)
    (init-state!)
    (mount-app-element!)))
