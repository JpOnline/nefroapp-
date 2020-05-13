(ns ^:figwheel-hooks nefroapp.main
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
    [nefroapp.telas.routing :as routing]
    [re-frame.core :as re-frame]
    [reagent.dom]
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
     '({:criada-em "2018-06-04T18:11:17.998-03:00[SYSTEM]",                            
       :editada-em "2019-01-14T10:35:57.080-02:00[SYSTEM]",                           
       :farmacos                                                                      
       {"Eritropoetina" {:prescricao "Infundir 300ml (Máximo: 600ml)"},               
        "Heparina" {:prescricao "600ml"},                                             
        "Hidróxido de Ferro" {:prescricao "600ml"},                                   
        "Darbepoietina"                                                               
        {:prescricao                                                                  
         "Via intravenosa: 1-4µg, 3 vezes por semana, após a diálise"},               
        "Gabapentina" {:prescricao "250ml"},                                          
        "Manitol" {:prescricao "500ml/min"},                                          
        "Amido Hidroxietílico" {:prescricao "125ml"},         
        "Calcitriol"                                                                  
        {:prescricao                                                                  
         "Via intravenosa: 1-4µg, 3 vezes por semana, após a diálise"}}}              
      {:criada-em "2018-08-30T08:20:05.893-03:00[SYSTEM]",                            
       :editada-em "2019-05-28T11:06:22.610-03:00[SYSTEM]",                           
       :farmacos                                                                      
       {"Dexclorfeniramina" {:prescricao "250ml"},                                    
        "Hidróxido de Ferro" {:prescricao "6mg"},                                     
        "Amido Hidroxietílico" {:prescricao "600ml"},                                 
        "Gabapentina" {:prescricao "300mg - 3 vezes por semana"},                     
        "Manitol" {:prescricao "Infundir 300ml (Máximo: 600ml)"},                     
        "Eritropoetina" {:prescricao "0,5-1µg de 2-3 vezes por semana"},              
        "Darbepoietina" {:prescricao "300mg - 3 vezes por semana"},                   
        "Enoxaparina" {:prescricao "125ml"},                                          
        "Calcitriol" {:prescricao "500ml/min"}}}                                      
      {:criada-em "2019-06-30T10:06:31.451-03:00[SYSTEM]",                            
       :editada-em "2018-07-31T09:42:47.171-03:00[SYSTEM]",                           
       :farmacos                                                                      
       {"Dexclorfeniramina" {:prescricao "600ml"},                                    
        "Hidróxido de Ferro"                                                          
        {:prescricao "0,5-1µg de 2-3 vezes por semana"},                              
        "Manitol" {:prescricao "500ml/min"},                                          
        "Darbepoietina"                                                               
        {:prescricao "Via oral: 0,5-1µg 3 vezes por semana"},                         
        "Eritropoetina" {:prescricao "500ml/min"},                                    
        "Amido Hidroxietílico" {:prescricao "6mg"}}}                                  
      {:criada-em "2018-12-28T21:10:18.564-02:00[SYSTEM]",                            
       :editada-em "2018-10-21T14:40:45.142-03:00[SYSTEM]",                           
       :farmacos                                                                      
       {"Heparina"                                                                    
        {:prescricao                                                                  
         "Via intravenosa: 1-4µg, 3 vezes por semana, após a diálise"},               
        "Hidróxido de Ferro" {:prescricao "600ml"},                                   
        "Dexclorfeniramina"                                        
        {:prescricao "0,5-1µg de 2-3 vezes por semana"},                              
        "Darbepoietina" {:prescricao "Infundir 300ml (Máximo: 600ml)"},               
        "Gabapentina"                                                                 
        {:prescricao                                                                  
         "Via intravenosa: 1-4µg, 3 vezes por semana, após a diálise"},               
        "Enoxaparina" {:prescricao "Infundir 300ml (Máximo: 600ml)"},                 
        "Manitol" {:prescricao "125ml"},                                              
        "Amido Hidroxietílico"                                                        
        {:prescricao                                                                  
         "Via intravenosa: 1-4µg, 3 vezes por semana, após a diálise"}}}              
      {:criada-em "2019-08-18T14:01:50.269-03:00[SYSTEM]",                            
       :editada-em "2018-09-28T20:11:07.971-03:00[SYSTEM]",                           
       :farmacos                                                                      
       {"Dexclorfeniramina" {:prescricao "300ml/min"},                                
        "Hidróxido de Ferro" {:prescricao "Via oral: 0,25-0,5µg/dia"},                
        "Gabapentina" {:prescricao "Infundir 300ml (Máximo: 600ml)"},                 
        "Manitol" {:prescricao "Via oral: 0,5-1µg 3 vezes por semana"},               
        "Eritropoetina" {:prescricao "0,5-1µg de 2-3 vezes por semana"},              
        "Darbepoietina" {:prescricao "Infundir 300ml (Máximo: 600ml)"},               
        "Heparina" {:prescricao "250ml"},                                             
        "Enoxaparina" {:prescricao "300mg - 3 vezes por semana"},                     
        "Calcitriol" {:prescricao "125ml"}}}                                          
      {:criada-em "2019-09-04T23:36:42.843-03:00[SYSTEM]",                            
       :editada-em "2019-10-08T14:12:26.004-03:00[SYSTEM]",                           
       :farmacos                                                                      
       {"Amido Hidroxietílico" {:prescricao "300mg - 3 vezes por semana"}}}           
      {:criada-em "2019-12-31T15:39:01.283-03:00[SYSTEM]",                            
       :editada-em "2020-04-06T21:45:37.814-03:00[SYSTEM]",                           
       :farmacos                                                                      
       {"Manitol" {:prescricao "300ml/min"},                                          
        "Amido Hidroxietílico"                                                        
        {:prescricao "Infundir 300ml (Máximo: 600ml)"},                               
        "Darbepoietina" {:prescricao "500ml/min"}}}                                   
      {:criada-em "2019-06-13T07:11:07.498-03:00[SYSTEM]",     
       :editada-em "2019-01-29T20:07:03.030-02:00[SYSTEM]",                           
       :farmacos                                                                      
       {"Hidróxido de Ferro"                                                          
        {:prescricao "0,5-1µg de 2-3 vezes por semana"},                              
        "Eritropoetina"                                                               
        {:prescricao                                                                  
         "Via intravenosa: 1-4µg, 3 vezes por semana, após a diálise"},               
        "Darbepoietina" {:prescricao "500ml/min"},                                    
        "Manitol" {:prescricao "125ml"},                                              
        "Dexclorfeniramina" {:prescricao "500ml/min"},                                
        "Calcitriol" {:prescricao "500ml/min"},                                       
        "Heparina"                                                                    
        {:prescricao                                                                  
         "Via intravenosa: 1-4µg, 3 vezes por semana, após a diálise"},               
        "Amido Hidroxietílico" {:prescricao "600ml"}}}                                
      {:criada-em "2020-03-03T11:12:47.146-03:00[SYSTEM]",                            
       :editada-em "2018-12-27T15:07:20.675-02:00[SYSTEM]",                           
       :farmacos                                                                      
       {"Enoxaparina" {:prescricao "300mg - 3 vezes por semana"},                     
        "Eritropoetina" {:prescricao "300mg - 3 vezes por semana"},                   
        "Manitol" {:prescricao "Via oral: 0,5-1µg 3 vezes por semana"},               
        "Amido Hidroxietílico"                                                        
        {:prescricao "0,5-1µg de 2-3 vezes por semana"},                              
        "Hidróxido de Ferro"                                                          
        {:prescricao                                                                  
         "Via intravenosa: 1-4µg, 3 vezes por semana, após a diálise"},               
        "Heparina" {:prescricao "500ml/min"},                                         
        "Gabapentina" {:prescricao "500ml/min"},                                      
        "Darbepoietina"                                                               
        {:prescricao                                                                  
         "Via intravenosa: 1-4µg, 3 vezes por semana, após a diálise"}}}              
    {:criada-em "2018-07-08T09:23:16.946-03:00[SYSTEM]",                            
     :editada-em "2019-01-28T16:17:53.093-02:00[SYSTEM]",                           
     :farmacos                                           
     {"Calcitriol" {:prescricao "300mg - 3 vezes por semana"},                      
      "Hidróxido de Ferro" {:prescricao "6mg"},                                     
      "Manitol"                                                                     
      {:prescricao                                                                  
       "Via intravenosa: 1-4µg, 3 vezes por semana, após a diálise"},               
      "Heparina" {:prescricao "125ml"}}}                                            
    {:criada-em "2019-03-25T19:24:50.468-03:00[SYSTEM]",                            
     :editada-em "2018-05-30T14:10:56.709-03:00[SYSTEM]",                           
     :farmacos                                                                      
     {"Manitol" {:prescricao "600ml"},                                              
      "Calcitriol" {:prescricao "250ml"},                                           
      "Eritropoetina" {:prescricao "6mg"},                                          
      "Enoxaparina" {:prescricao "600ml"},                                          
      "Amido Hidroxietílico" {:prescricao "300mg - 3 vezes por semana"}}}           
    {:criada-em "2020-01-16T22:50:44.232-03:00[SYSTEM]",                            
     :editada-em "2019-03-05T18:43:05.725-03:00[SYSTEM]",                           
     :farmacos                                                                      
     {"Manitol"                                                                     
      {:prescricao                                                                  
       "Via intravenosa: 1-4µg, 3 vezes por semana, após a diálise"},               
      "Calcitriol" {:prescricao "500ml/min"},                                       
      "Gabapentina" {:prescricao "125ml"},                                          
      "Eritropoetina" {:prescricao "600ml"},                                        
      "Enoxaparina" {:prescricao "300ml/min"}}}                                     
    {:criada-em "2020-02-25T07:46:48.588-03:00[SYSTEM]",                            
     :editada-em "2018-08-27T12:06:33.816-03:00[SYSTEM]",                           
     :farmacos                                                                      
     {"Manitol" {:prescricao "Via oral: 0,25-0,5µg/dia"},                           
      "Enoxaparina" {:prescricao "300ml/min"},                                      
      "Gabapentina" {:prescricao "500ml/min"},                                      
      "Darbepoietina" {:prescricao "Via oral: 0,25-0,5µg/dia"}}}                    
    {:criada-em "2018-12-24T21:39:05.787-02:00[SYSTEM]",                            
     :editada-em "2019-05-17T13:05:11.405-03:00[SYSTEM]",                           
     :farmacos                                                   
     {"Calcitriol" {:prescricao "6mg"},                                             
      "Heparina" {:prescricao "Via oral: 0,25-0,5µg/dia"},                          
      "Enoxaparina" {:prescricao "Infundir 300ml (Máximo: 600ml)"}}}                
    {:criada-em "2018-11-13T21:22:55.453-02:00[SYSTEM]",                            
     :editada-em "2019-08-02T16:29:31.041-03:00[SYSTEM]",                           
     :farmacos                                                                      
     {"Darbepoietina"                                                               
      {:prescricao                                                                  
       "Via intravenosa: 1-4µg, 3 vezes por semana, após a diálise"}}}              
    {:criada-em "2019-01-24T20:59:53.587-02:00[SYSTEM]",                            
     :editada-em "2019-05-15T10:56:51.851-03:00[SYSTEM]",                           
     :farmacos                                                                      
     {"Manitol" {:prescricao "300mg - 3 vezes por semana"},                         
      "Eritropoetina" {:prescricao "125ml"}}}                                       
    {:criada-em "2019-07-17T18:18:05.534-03:00[SYSTEM]",                            
     :editada-em "2019-05-16T12:00:07.969-03:00[SYSTEM]",                           
     :farmacos                                                                      
     {"Hidróxido de Ferro" {:prescricao "600ml"},                                   
      "Heparina" {:prescricao "600ml"},                                             
      "Manitol" {:prescricao "Via oral: 0,5-1µg 3 vezes por semana"},               
      "Gabapentina" {:prescricao "Via oral: 0,25-0,5µg/dia"},                       
      "Eritropoetina" {:prescricao "300ml/min"},                                    
      "Enoxaparina" {:prescricao "300mg - 3 vezes por semana"}}}                    
    {:criada-em "2019-12-31T09:58:56.726-03:00[SYSTEM]",                            
     :editada-em "2019-02-15T19:25:26.363-02:00[SYSTEM]",                           
     :farmacos                                                                      
     {"Dexclorfeniramina" {:prescricao "6mg"},                                      
      "Calcitriol" {:prescricao "125ml"},                                           
      "Manitol" {:prescricao "600ml"},                                              
      "Eritropoetina"                                                               
      {:prescricao                                                                  
       "Via intravenosa: 1-4µg, 3 vezes por semana, após a diálise"},               
      "Enoxaparina"                                                                 
      {:prescricao                                                   
       "Via intravenosa: 1-4µg, 3 vezes por semana, após a diálise"},               
      "Heparina" {:prescricao "125ml"},                                             
      "Hidróxido de Ferro"                                                          
      {:prescricao "Infundir 300ml (Máximo: 600ml)"}}}                              
    {:criada-em "2019-05-18T15:42:52.486-03:00[SYSTEM]",                            
     :editada-em "2019-05-03T23:12:45.137-03:00[SYSTEM]",                           
     :farmacos                                                                      
     {"Eritropoetina" {:prescricao "6mg"},                                          
      "Enoxaparina" {:prescricao "500ml/min"},                                      
      "Hidróxido de Ferro"                                                          
      {:prescricao "Via oral: 0,5-1µg 3 vezes por semana"},                         
      "Dexclorfeniramina" {:prescricao "125ml"},                                    
      "Calcitriol" {:prescricao "0,5-1µg de 2-3 vezes por semana"}}}                
    {:criada-em "2020-02-20T07:35:05.100-03:00[SYSTEM]",                            
     :editada-em "2018-08-26T11:02:00.359-03:00[SYSTEM]",                           
     :farmacos {}}                                                                  
    {:criada-em "2018-06-06T19:00:10.059-03:00[SYSTEM]",                            
     :editada-em "2018-07-07T13:02:42.103-03:00[SYSTEM]",                           
     :farmacos                                                                      
     {"Calcitriol" {:prescricao "500ml/min"},                                       
      "Amido Hidroxietílico" {:prescricao "600ml"},                                 
      "Darbepoietina" {:prescricao "300mg - 3 vezes por semana"},                   
      "Eritropoetina" {:prescricao "500ml/min"},                                    
      "Enoxaparina" {:prescricao "Infundir 300ml (Máximo: 600ml)"},                 
      "Gabapentina"                                                                 
      {:prescricao                                                                  
       "Via intravenosa: 1-4µg, 3 vezes por semana, após a diálise"},               
      "Hidróxido de Ferro" {:prescricao "125ml"},
      "Dexclorfeniramina" {:prescricao "500ml/min"}}}                               
    {:criada-em "2019-11-24T20:30:39.955-03:00[SYSTEM]",                            
     :editada-em "2018-08-26T10:21:20.651-03:00[SYSTEM]",
     :farmacos                                                                      
     {"Gabapentina" {:prescricao "Via oral: 0,5-1µg 3 vezes por semana"},           
      "Manitol" {:prescricao "300mg - 3 vezes por semana"},      
      "Eritropoetina" {:prescricao "0,5-1µg de 2-3 vezes por semana"},              
      "Amido Hidroxietílico" {:prescricao "500ml/min"},                             
      "Darbepoietina" {:prescricao "Via oral: 0,25-0,5µg/dia"},                     
      "Dexclorfeniramina" {:prescricao "300mg - 3 vezes por semana"},               
      "Heparina" {:prescricao "125ml"}}}                                            
    {:criada-em "2019-12-04T11:09:46.363-03:00[SYSTEM]",                            
     :editada-em "2019-10-27T15:41:53.563-03:00[SYSTEM]",                           
     :farmacos                                                                      
     {"Manitol" {:prescricao "Via oral: 0,5-1µg 3 vezes por semana"},               
      "Darbepoietina" {:prescricao "Infundir 300ml (Máximo: 600ml)"},               
      "Heparina" {:prescricao "Via oral: 0,5-1µg 3 vezes por semana"},              
      "Eritropoetina" {:prescricao "Infundir 300ml (Máximo: 600ml)"}}}              
    {:criada-em "2018-12-05T21:37:28.823-02:00[SYSTEM]",                            
     :editada-em "2018-06-02T16:39:28.080-03:00[SYSTEM]",                           
     :farmacos                                                                      
     {"Eritropoetina"                                                               
      {:prescricao                                                                  
       "Via intravenosa: 1-4µg, 3 vezes por semana, após a diálise"},               
      "Amido Hidroxietílico" {:prescricao "250ml"},                                 
      "Darbepoietina" {:prescricao "500ml/min"},
      "Hidróxido de Ferro"                                                          
      {:prescricao "0,5-1µg de 2-3 vezes por semana"},                              
      "Manitol" {:prescricao "600ml"},                                              
      "Enoxaparina" {:prescricao "500ml/min"},                                      
      "Gabapentina" {:prescricao "300ml/min"}}}                                     
    {:criada-em "2018-08-03T12:01:46.524-03:00[SYSTEM]",                            
     :editada-em "2020-02-14T18:58:58.081-03:00[SYSTEM]",                           
     :farmacos    
     {"Dexclorfeniramina" {:prescricao "125ml"},                                    
      "Hidróxido de Ferro" {:prescricao "250ml"},                                   
      "Amido Hidroxietílico"              
      {:prescricao "Via oral: 0,5-1µg 3 vezes por semana"},                         
      "Gabapentina" {:prescricao "250ml"},                                          
      "Manitol" {:prescricao "6mg"},      
      "Eritropoetina" {:prescricao "Infundir 300ml (Máximo: 600ml)"},               
      "Darbepoietina"                                                               
      {:prescricao                                                                  
       "Via intravenosa: 1-4µg, 3 vezes por semana, após a diálise"},               
      "Heparina" {:prescricao "Via oral: 0,25-0,5µg/dia"},
      "Enoxaparina" {:prescricao "125ml"},                                          
      "Calcitriol" {:prescricao "Infundir 300ml (Máximo: 600ml)"}}}                 
    {:criada-em "2019-04-10T17:42:31.885-03:00[SYSTEM]",                            
     :editada-em "2019-06-03T12:36:55.140-03:00[SYSTEM]",                           
     :farmacos                                                                      
     {"Eritropoetina" {:prescricao "250ml"},                                        
      "Dexclorfeniramina" {:prescricao "600ml"}}}                                   
    {:criada-em "2019-06-22T23:38:52.797-03:00[SYSTEM]",                            
     :editada-em "2020-03-09T11:00:23.579-03:00[SYSTEM]",                           
     :farmacos                                                                      
     {"Amido Hidroxietílico" {:prescricao "125ml"},                                 
      "Gabapentina" {:prescricao "0,5-1µg de 2-3 vezes por semana"},                
      "Darbepoietina" {:prescricao "500ml/min"},                                    
      "Hidróxido de Ferro" {:prescricao "125ml"},                                   
      "Calcitriol" {:prescricao "600ml"},
      "Enoxaparina" {:prescricao "250ml"}}}                                         
    {:criada-em "2019-03-17T06:30:19.259-03:00[SYSTEM]",                            
     :editada-em "2018-04-21T14:58:07.366-03:00[SYSTEM]",                           
     :farmacos                                                                      
     {"Gabapentina" {:prescricao "Infundir 300ml (Máximo: 600ml)"},                 
      "Heparina" {:prescricao "300mg - 3 vezes por semana"},                        
      "Manitol"                                                                     
      {:prescricao
       "Via intravenosa: 1-4µg, 3 vezes por semana, após a diálise"},
      "Dexclorfeniramina" {:prescricao "Infundir 300ml (Máximo: 600ml)"},           
      "Darbepoietina" {:prescricao "6mg"},
      "Eritropoetina" {:prescricao "300ml/min"},                                    
      "Calcitriol" {:prescricao "Infundir 300ml (Máximo: 600ml)"},
      "Amido Hidroxietílico" {:prescricao "300mg - 3 vezes por semana"}}}
    {:criada-em "2018-07-01T18:12:50.260-03:00[SYSTEM]",
     :editada-em "2019-11-03T16:48:35.110-03:00[SYSTEM]",                           
     :farmacos                                                                      
     {"Hidróxido de Ferro" {:prescricao "300mg - 3 vezes por semana"},              
      "Manitol" {:prescricao "300ml/min"},
      "Gabapentina" {:prescricao "125ml"},                                          
      "Enoxaparina" {:prescricao "6mg"},                                            
      "Calcitriol" {:prescricao "600ml"},                                           
      "Heparina" {:prescricao "6mg"},                                               
      "Dexclorfeniramina"                                                           
      {:prescricao "Via oral: 0,5-1µg 3 vezes por semana"},
      "Darbepoietina" {:prescricao "Via oral: 0,25-0,5µg/dia"}}}
    {:criada-em "2020-04-11T13:34:45.101-03:00[SYSTEM]",                            
     :editada-em "2018-08-14T15:01:30.865-03:00[SYSTEM]",
     :farmacos                                                                      
     {"Manitol" {:prescricao "0,5-1µg de 2-3 vezes por semana"},                    
      "Dexclorfeniramina" {:prescricao "Infundir 300ml (Máximo: 600ml)"},
      "Amido Hidroxietílico" {:prescricao "300mg - 3 vezes por semana"},            
      "Hidróxido de Ferro" {:prescricao "6mg"},
      "Heparina" {:prescricao "125ml"}}}
    {:criada-em "2019-08-30T12:21:29.295-03:00[SYSTEM]",                            
     :editada-em "2019-07-03T07:12:13.631-03:00[SYSTEM]",                           
     :farmacos                                                                      
     {"Eritropoetina" {:prescricao "250ml"},
      "Heparina" {:prescricao "Infundir 300ml (Máximo: 600ml)"}}}
    {:criada-em "2019-10-18T21:45:42.710-03:00[SYSTEM]",
     :editada-em "2019-06-19T13:34:00.895-03:00[SYSTEM]",                           
     :farmacos 
     {"Gabapentina" {:prescricao "250ml"},
      "Hidróxido de Ferro" {:prescricao "6mg"},                                     
      "Darbepoietina" {:prescricao "6mg"},
      "Eritropoetina"                                                               
      {:prescricao
       "Via intravenosa: 1-4µg, 3 vezes por semana, após a diálise"},
      "Enoxaparina"
      {:prescricao "Via oral: 0,5-1µg 3 vezes por semana"}}}                        
    {:criada-em "2018-12-21T19:17:37.582-02:00[SYSTEM]",                            
     :editada-em "2019-03-31T12:00:13.893-03:00[SYSTEM]",                           
     :farmacos
     {"Eritropoetina" {:prescricao "300ml/min"},                                    
      "Gabapentina" {:prescricao "Via oral: 0,25-0,5µg/dia"},                       
      "Manitol" {:prescricao "Via oral: 0,25-0,5µg/dia"},                           
      "Heparina" {:prescricao "Via oral: 0,25-0,5µg/dia"},                          
      "Hidróxido de Ferro" {:prescricao "Via oral: 0,25-0,5µg/dia"},                
      "Darbepoietina"
      {:prescricao
       "Via intravenosa: 1-4µg, 3 vezes por semana, após a diálise"},               
      "Calcitriol" {:prescricao "250ml"}}}
    {:criada-em "2018-11-11T23:31:24.026-02:00[SYSTEM]",                            
     :editada-em "2019-11-17T06:36:07.395-03:00[SYSTEM]",                           
     :farmacos
     {"Eritropoetina" {:prescricao "125ml"},                                        
      "Manitol" {:prescricao "500ml/min"},
      "Calcitriol" {:prescricao "6mg"},
      "Hidróxido de Ferro" {:prescricao "6mg"}}}                                    
    {:criada-em "2018-08-12T11:28:43.664-03:00[SYSTEM]",                            
     :editada-em "2020-04-06T14:11:52.304-03:00[SYSTEM]",                           
     :farmacos
     {"Heparina" {:prescricao "300ml/min"},
      "Gabapentina" {:prescricao "125ml"},
      "Darbepoietina" {:prescricao "300mg - 3 vezes por semana"},                   
      "Manitol"
      {:prescricao
       "Via intravenosa: 1-4µg, 3 vezes por semana, após a diálise"},               
      "Amido Hidroxietílico"
      {:prescricao "Infundir 300ml (Máximo: 600ml)"}}})}
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
    (re-frame/clear-subscription-cache!))
  (when-let [el (.getElementById js/document "app")]
    (reagent.dom/render [routing/selected-view] el)))

(defonce startup
  (do
    (register-service-worker!)
    (init-state!)
    (mount-app-element!)))
