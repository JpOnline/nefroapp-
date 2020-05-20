(ns nefroapp.telas.receita
  (:require
    [button :as material-button]
    [chevron-left-icon :as mui-icon-chevron-left]
    [chevron-right-icon :as mui-icon-chevron-right]
    [icon-button :as material-icon-button]
    [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
    [nefroapp.domain.receita-historico :as receita-historico]
    [nefroapp.telas.shell-components :as shell]
    [nefroapp.util :as util :refer [<sub >evt]]
    [re-frame.core :as re-frame]
    [tick.alpha.api :as tick]
    ;; [List :as material-list]
    ;; [ListItem :as material-list-item]
    ;; [ListItemText :as material-list-item-text]
    ;; [SearchIcon :as mui-icon-search]

    [reagent.core :as reagent]
    ))

(defn-traced select-history-value-before
  [app-state [event farmaco-nome total]]
  (update-in app-state [:ui :receita :historico farmaco-nome]
             #(-> % inc (min total))))
(re-frame/reg-event-db ::select-history-value-before select-history-value-before)

(defn-traced select-history-value-after
  [app-state [event farmaco-nome]]
  (update-in app-state [:ui :receita :historico farmaco-nome]
             #(-> % dec (max 0))))
(re-frame/reg-event-db ::select-history-value-after select-history-value-after)

(defn receita-historico-selecionado
  [app-state]
  (get-in app-state [:ui :receita :historico] {}))
(re-frame/reg-sub
  ::receita-historico-selecionado
  receita-historico-selecionado)

(defn today [] "2020-04-23T13:13:23.826-03:00[SYSTEM]" #_(str (tick/zoned-date-time)))

(defn get-paciente-selecionado-receitas [app-state]
  (let [paciente-selecionado (get-in app-state [:ui :paciente-selecionado] 0)
        receitas (get-in app-state [:domain :pacientes paciente-selecionado :receitas] nil)
        sorted-receitas (reverse (sort-by :criada-em receitas)) ;; TODO: Precisa ordenar? Avaliar.

        get-date #(second (when % (re-find #"(\d{4}\D\d{1,2}\D\d{1,2})" %)))
        tem-receita-de-hoje? (= (get-date (today))
                                (get-date (:criada-em (first sorted-receitas))))

        ;; Vou usar a receita de hoje para mostrar o valor de prescrição no input.
        todays-receita (when tem-receita-de-hoje? (first sorted-receitas))

        ;; A receita de hoje é a que está sendo editada, então não vou
        ;; considerá-la pra criar o histórico.
        sorted-receitas (if tem-receita-de-hoje?
                          (rest sorted-receitas)
                          sorted-receitas)]
    {:todays-receita todays-receita
     :sorted-receitas sorted-receitas}))

(defn set-todays-receita
  [app-state [event farmaco-nome new-value]]
  (let [{:keys [todays-receita sorted-receitas]} (get-paciente-selecionado-receitas app-state)
        farmacos-sem-prescricao (into {} (for [[farmaco-nome _] (:farmacos (first sorted-receitas))] {farmaco-nome {:prescricao ""}}))
        updated-todays-receita (if todays-receita
                                 (-> todays-receita
                                     (assoc :editada-em (today))
                                     (assoc-in [:farmacos farmaco-nome :prescricao] new-value))
                                 {:criada-em (today)
                                  :editada-em (today)
                                  :farmacos (assoc-in farmacos-sem-prescricao [farmaco-nome :prescricao] new-value)})
        paciente-selecionado (get-in app-state [:ui :paciente-selecionado] 0)]
    (-> app-state
        (assoc-in [:ui :receita :editando farmaco-nome] false)
        (assoc-in [:ui :receita :on-focus farmaco-nome] false)
        (assoc-in [:domain :pacientes paciente-selecionado :receitas]
                  (vec (concat [updated-todays-receita] sorted-receitas))))))
(re-frame/reg-event-db ::set-todays-receita set-todays-receita)

(defn farmacos-lista
  [app-state]
  (let [;; Vou usar a receita de hoje para mostrar o valor de prescrição no input.
        ;; A receita de hoje é a que está sendo editada, então não vou
        ;; considerá-la pra criar o histórico.
        {:keys [todays-receita sorted-receitas]} (get-paciente-selecionado-receitas app-state)

        ;; Cria algo do tipo
          ;; {"farmacoA" [{:data "2020-01-01" :value "10ml"}
          ;;              {:data "2020-02-01" :value "20ml"}]
          ;;  "farmacoB" [{:data "2020-01-01" :value "50ml"}
          ;;              {:data "2020-02-01" :value "70ml"}]}
        farmacos->date-values-map (fn [criada-em farmacos]
                                    (into {} (for [[nome {:keys [prescricao]}] farmacos]
                                               {nome [{:date criada-em :value prescricao}]})))

        farmacos-history (reduce (fn [acc {:keys [criada-em farmacos]}]
                                   (merge-with concat acc
                                               (farmacos->date-values-map criada-em farmacos)))
                                 {} sorted-receitas)]
    (into [] (for [[farmaco-nome historico] farmacos-history]
               {:nome farmaco-nome
                :historico (receita-historico/compact-history
                             (today)
                             historico)
                :prescricao-de-hoje (some-> todays-receita :farmacos
                                            (get farmaco-nome) :prescricao)}))))
(re-frame/reg-sub ::farmacos-lista farmacos-lista)

(def left {:display "flex"
           :marginBottom "7px"
           :position "absolute"
           :transform "translateX(-200%)"
           :clipPath "inset(0px 0px 0px 200%)"
           :transition "0.7s linear"})

(def middle {:display "flex"
             :marginBottom "7px"
             :transform "translateX(0px)"
             :position "absolute"
             :clipPath "inset(0px)"
             :transition "0.7s linear"})

(def right {:display "flex"
            :marginBottom "7px"
            :position "absolute"
            :transform "translateX(200%)"
            :clipPath "inset(0px 200% 0px 0px)"
            :transition "0.7s linear"}) ;; TODO: Animação ainda não está muito boa.

(defn titulo-farmaco [titulo]
  [:h3
   {:style {:marginBottom "5px"}}
   titulo])

(defn historico-farmaco [{:keys [historico nome]}]
  (let [idx-selecionado (or ((<sub [::receita-historico-selecionado]) nome) 0)
        last-idx (dec (count historico))
        idx-- (max (dec idx-selecionado) 0)
        idx++ (min (+ 2 idx-selecionado) (count historico))]
    [:div
     {:style {:transform "translateY(-5px)"
              :position "relative"}}
     (for [idx (range idx-- idx++)]
       ^{:key (str nome idx)}
       [:div
        (condp #(%1 %2 idx) idx-selecionado
            < {:style left}
            > {:style right}
            = {:style middle}
            {:style left})
        (when (< idx last-idx)
          [:> material-icon-button
           {:color "inherit"
            :onClick #(>evt [::select-history-value-before nome last-idx])}
           [:> mui-icon-chevron-left]])
        [:p {:style {:color "gray"}}
         (nth historico idx nil)]
        (when (> idx 0)
          [:> material-icon-button
           {:color "inherit"
            :onClick #(>evt [::select-history-value-after nome])}
           [:> mui-icon-chevron-right]])])
     [:p.placeholder-for-height-computation
      {:style {:padding "0px 48px"
               :visibility "hidden"
               ;; :transition "max-height 4s ease-in-out"
               ;; :overflow "hidden"
               ;; :height "auto"
               ;; :maxHeight "100%"
               ;; ;; :maxHeight "0px"
               }}
      (nth historico idx-selecionado)]]))

(defn input-farmaco [{:keys [value onBlur onClick]}]
  [:div
   {:style {:border "1px solid #BDBDBD"
            :borderRadius "8px"
            :width "100%"
            :padding "0px 3px"}}
   [:nefroapp-textarea
    {:style {:width "100%"
             :marginTop "-18px"}
     :placeholder "Prescrição de hoje"
     :value value
     :onClick onClick
     :onBlur onBlur}]])

(defn farmaco-button [{:keys [onClick]} text]
  [:> material-button
   {:style {:marginLeft "10px"}
    :onClick onClick
    :variant "contained"
    :size "small"}
   text])

(defn set-input-focus
  [app-state [event farmaco-nome value]]
  ;; TODO: Refatorar para colocar os farmacos num map que pode dar merge no
  ;; farmaco do domain.
  (assoc-in app-state [:ui :receita :on-focus farmaco-nome] value))
(re-frame/reg-event-db ::set-input-focus set-input-focus)

(defn on-focus?
  [app-state]
  ;; TODO: Refatorar quando puder dar merge no farmaco domain e ui
  (get-in app-state [:ui :receita :on-focus] {}))
(re-frame/reg-sub ::on-focus? on-focus?)

(defn input-field [{:keys [prescricao-de-hoje nome historico-selecionado]} button]
  [:div
   {:style {:display "flex"
            :alignItems "center"}}
   [input-farmaco
    {:value prescricao-de-hoje
     :onClick #(>evt [::set-input-focus nome true])
     :onBlur #(>evt [::set-todays-receita nome (-> % .-target .-value)])}]
   button])

(defn edit-farmaco
  [app-state [event farmaco-nome]]
  ;; TODO: Refatorar para colocar os farmacos num map que pode dar merge no
  ;; farmaco do domain.
  (assoc-in app-state [:ui :receita :editando farmaco-nome] true))
(re-frame/reg-event-db ::edit-farmaco edit-farmaco)

(defn prescricao-hoje-line [{:keys [prescricao-de-hoje nome]}]
  [:div
   {:style {:display "flex"
            :alignItems "center"}}
   [:div prescricao-de-hoje]
   [:> material-button
    {:style {:marginLeft "10px"}
     :onClick #(>evt [::edit-farmaco nome])
     :variant "contained"
     :size "small"}
    "Editar"]])

(defn editando?
  [app-state]
  ;; TODO: Refatorar quando puder dar merge no farmaco domain e ui
  (get-in app-state [:ui :receita :editando] {}))
(re-frame/reg-sub ::editando? editando?)

(defn farmaco-component [{{:keys [historico nome prescricao-de-hoje]} :farmaco}]
  (let [idx-selecionado (or ((<sub [::receita-historico-selecionado]) nome) 0)
        historico-selecionado (as-> historico $
                                  (nth $ idx-selecionado)
                                  (clojure.string/split $ #"- ")
                                  (rest $)
                                  (clojure.string/join "- " $))]
    [:<>
     [titulo-farmaco nome]
     ;; TODO: Colocar essa lógica do "and" num único reg-sub quando for
     ;; possível dar merge nos farmacos do domain e ui.
     (cond
       ;; TODO: Preciso indicar de alguma outra forma quando a prescrição é vazia?
       (and prescricao-de-hoje (not= prescricao-de-hoje "") (not ((<sub [::editando?]) nome)))
         [prescricao-hoje-line
          {:prescricao-de-hoje prescricao-de-hoje
           :nome nome}]
       ((<sub [::on-focus?]) nome)
         [:<>
          [historico-farmaco
           {:historico historico
            :nome nome}]
          [input-field
           {:prescricao-de-hoje prescricao-de-hoje
            :nome nome
            :historico-selecionado historico-selecionado}
           [farmaco-button
            {}
            "Ok"]]]
       :else
         [:<>
          [historico-farmaco
           {:historico historico
            :nome nome}]
          [input-field
           {:prescricao-de-hoje prescricao-de-hoje
            :nome nome
            :historico-selecionado historico-selecionado}
           [farmaco-button
            {:onClick #(>evt [::set-todays-receita nome historico-selecionado])}
            "Repetir"]]])]))

(defn a4-grid []
  [:<>
   (map (fn [top]
          ^{:key top}
          [:div
           {:style {:top (str top "mm")
                    :position "absolute"
                    :width "210mm"
                    :height "1px"
                    :backgroundColor "gray"
                    }}])
        [26 (+ 26 245)])
   (map (fn [left]
          ^{:key left}
          [:div
           {:style {:left (str left "mm")
                    :position "absolute"
                    :width "1px"
                    :height "297mm"
                    :backgroundColor "gray"
                    }}])
        [30 (+ 30 150)])])

(defn printing-component []
  (let [top-margin 25
        ;; bottom-margin (+ 26 245)
        ;; right-margin (+ 30 150)
        left-margin 30]
    [:div
     {:style {:position "relative"
              "WebkitPrintColorAdjust" "exact"}}
     ;; [a4-grid]
     [:div
      {:style {:position "absolute"
               :left (str left-margin"mm")
               :top (str top-margin"mm")
               :fontWeight "bold"}}
      [:div
       {:style {:display "flex"
                :alignItems "center"
                :width "150mm"
                :placeContent "center"}}
       [:img
        {:src "images/logo-hospital.png"
         :style {:width "26.7mm"}}]
       [:h4 "SERVIÇO DE NEFROLOGIA DE MATÃO"]
       [:img
        {:src "images/imagem-rim.png"
         :style {:width "13mm"}}]]
      [:u [:h3
           {:style {:textAlign "center"}}
           "PRESCRIÇÃO DE HEMODIÁLISE - FEVEREIRO DE 2020"]] ;; TODO: Mudar a data
      [:h2 "PACIENTE: JAQUELINE ELIZIARIO"] ;; TODO: Mudar o dado
      [:div {:style {:fontSize "10pt"}}
       [:p "UF Máxima: 3500 ML"]
       [:p "Temperatura: 36°C"]
       [:p "Tempo: 4 HORAS"] ;; TODO: Mudar o dado
       [:p "Ca: 3 meq/L"]
       [:p "Glicose: sim"]
       [:p "Capilar: Elisio 21 H"]
       [:p "Peso Seco: 61,5 KG"]
       [:p "Fluxo de Sangue: 400 ml/min"]
       [:p "Fluxo de Dialisato: 500 ml/min"]
       [:p "Na: 138 meq/L"]
       [:p "Acesso Vascular: Cateter de longa permanência em VJID"]
       [:p "Heparina: 1,2 ml"]
       [:p "BIC: -4"]
       [:p "KCL: 2,0  meq/L"]
       ]
      [:div {:style {:width "150mm"
                     :fontSize "10pt"
                     :textAlign "center"
                     :marginTop "64px"}}
       [:div "Rodrigo Santana Gonçalves da Silva"]
       [:div "CRM 145719"]]]]))

(defn printing-view []
  [:div#show-only-on-printing [printing-component]])

(defn component []
  [:div
   {:style {:overflowX "hidden"}}
   (when (empty? (<sub [::farmacos-lista]))
     [:h3 "Nenhum medicamento cadastrado ainda."])
   (for [farmaco (<sub [::farmacos-lista])] ;; TODO: Dar merge com informações da UI
     ^{:key (:nome farmaco)}
     [farmaco-component
      {:farmaco farmaco}])])

(defn hide-on-print-style []
  [:style
   (str
     "#show-only-on-printing {
       display: none;
     }
     @media print {
       #show-only-on-printing ~ * {"
         "display: none !important;
       }
       #show-only-on-printing {
         display: block;
       }
     }")])

(defn view []
  [:<>
   [hide-on-print-style]
   [printing-view]
   [shell/default
    [shell/error-boundary
     {:if-error [:h1 "Erro receita"]} ;; TODO: replace error-view
     [component]]]])
