(ns nefroapp.storage-module.firebase
  (:require
    [firebase :as fb]
    ))

(def firebase-config
  #js {:apiKey "AIzaSyAaFnmgQYCanR_fG2EkTJRNFdAAsrr8ojY"
       :authDomain "receita-nefro.firebaseapp.com"
       :databaseURL "https://receita-nefro.firebaseio.com"
       :projectId "receita-nefro"
       ;; :storageBucket ""
       ;; :messagingSenderId ""
       :appId "1:90181093295:web:35a89ec3ee054f2204115a"
       :measurementId "G-L12PX42LSM"})

(def firebase-db
  (do
    (when (= 0 (-> fb .-apps .-length))
      (-> fb (.initializeApp firebase-config)))
    (-> fb (.database))))

(defn save! [path value]
  (let [json (clj->js value)]
    (assert
      (do (js/console.log "compare" value (js->clj json :keywordize-keys true))
          (= value (js->clj json :keywordize-keys true)))
      "The given map is different if converted back from JSON.")
    (-> firebase-db (.ref path)
        (.set json #(when % (js/console.log "Erro ao gravar no Firebase." %))))))

(defn async-load [path callback-fn]
  (-> firebase-db (.ref path) (.once "value"
     (fn [snapshot]
       (callback-fn (some-> snapshot
                            (.val)
                            (js->clj :keywordize-keys true))))
     (fn [error]
       (js/console.log "Erro ao ler dados do Firebase." error)))))

#_(re-frame/reg-event-fx
  ::restore-domain-from-firebase
  (fn-traced
    [{:keys [db]} _]
    (if-let [user (some-> fb .auth .-currentUser)]
      (let [user-email (.-email user)
            name-in-email (first (clojure.string/split user-email "@"))
            user-fb-uid (.-uid user)]
        (-> firebase-db
            (.ref (str "users/"name-in-email"-"user-fb-uid))
            (.once "value"
                   (fn [snapshot]
                     (re-frame/dispatch-sync
                       [::restore-domain-from-firebase-callback snapshot]))))
        {:db (assoc-in db [:ui :state] "loading")})
      {:db (assoc-in db [:ui :state] "login")})))

#_(re-frame/reg-event-fx
  ::restore-domain-from-firebase-callback
  (fn-traced
    [_ [_ snapshot]]
    (let [restored-from-firebase (some-> snapshot
                                         (.val)
                                         (js->clj :keywordize-keys true))
          restored-state (merge restored-from-firebase
                                initial-state/ui-initial-state
                                {:authentication {:user-email (-> fb .auth .-currentUser .-email)}})
          default-state (merge initial-state/domain-initial-state
                               initial-state/ui-initial-state
                               {:authentication {:user-email (-> fb .auth .-currentUser .-email)}})]
      (if restored-from-firebase
        {:db restored-state
         :store restored-state}
        (do
          (js/console.log "Nenhum dado encontrado no firebase. Portanto o app-state foi reiniciado.")
          {:db default-state
           :store default-state})))))
