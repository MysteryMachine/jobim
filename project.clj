(defproject jobim "1.0.0"
  :description "Presentations in cljs!"
  :url "https://github.com/MysteryMachine/jobim"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.clojure/core.async "0.2.374"]
                 [com.cemerick/piggieback "0.2.1"]
                 [reagent "0.5.0"]
                 [fipp "0.6.4"]
                 [jobim-figwheel "0.1.0"]]

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0-1"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [{:id "test"
                :source-paths ["src" "test"]
                :figwheel true
                :compiler {:output-to "resources/public/js/compiled/test/test.js"
                           :output-dir "resources/public/js/compiled/test/out"
                           :asset-path "js/compiled/test/out"
                           :main jobim.core-test
                           :source-map true
                           :cache-analysis true}}]}

  :figwheel {:css-dirs ["resources/public/css"]
             :nrepl-port 7888
             :nrepl-middleware ["cider.nrepl/cider-middleware"
                                "refactor-nrepl.middleware/wrap-refactor"
                                "cemerick.piggieback/wrap-cljs-repl"]})
