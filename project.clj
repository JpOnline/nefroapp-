(defproject react-med.core "0.1.0-SNAPSHOT"
  :description "Web App para gerenciamento de receitas de pacientes médicos."
  ;; :url "http://jponline.github.io/site"

  :min-lein-version "2.9.3"

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.597"]
                 [com.cognitect/transit-cljs "0.8.264"]

                 ;; Has calendar dates, without times and timezones.
                 [tick "0.4.23-alpha"]

                 [re-frame "0.12.0"]
                 [reagent "0.10.0"]

                 ;; Should be production only
                 ;; [day8.re-frame/tracing-stubs "0.5.3"]
                 ]

  :source-paths ["src"]

  :aliases {"fig"       ["trampoline" "run" "-m" "figwheel.main"]
            "fig:cards" ["trampoline" "run" "-m" "figwheel.main" "-b" "cards" "-r"]
            "fig:cards-togithub"   ["run" "-m" "figwheel.main" "-O" "none" "-bo" "cards"]
            "fig:dev" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]
            "fig:none-prod" ["run" "-m" "figwheel.main" "-O" "none" "-bo" "prod"]
            "fig:prod" ["run" "-m" "figwheel.main" "-O" "advanced" "-bo" "prod"]}

  :profiles {:dev
             {:resource-paths ["target"]
              :clean-targets ^{:protect false} ["target"]
              :dependencies [[com.bhauman/figwheel-main "0.2.0"]
                             [com.bhauman/rebel-readline-cljs "0.1.4"]
                             [devcards "0.2.6"]
                             [day8.re-frame/re-frame-10x "0.6.0"]
                             [day8.re-frame/tracing "0.5.3"]
                             [org.clojure/test.check "0.10.0"]
                             ]
              :repl-options  {:port 12344}}
             :cards
             {:resource-paths ["target"]
              :clean-targets ^{:protect false} ["target"]
              :dependencies [[com.bhauman/figwheel-main "0.2.0"]
                             [com.bhauman/rebel-readline-cljs "0.1.4"]
                             [devcards "0.2.6"]
                             [day8.re-frame/re-frame-10x "0.6.0"]
                             [day8.re-frame/tracing "0.5.3"]
                             [org.clojure/test.check "0.10.0"]]}})
