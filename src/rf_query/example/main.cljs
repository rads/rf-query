(ns rf-query.example.main
  (:require
    [cljs.core.async :as async]
    [cljs.pprint :as pprint]
    [re-frame.core :as rf]
    [reagent.dom.client :as rdom]
    [rf-query.core :as rq]
    [rf-query.example.queries :as queries]))

(def foo-mutation
  {:mutation-fn (fn [_] (println "hey") (js/Promise.resolve {}))
   :on-success (fn [_] (println "mutated"))})

(def hello
  (rq/with-queries
    [queries/counter]
    (fn [_]
      (let [query @(rf/subscribe [::rq/query-state queries/counter])
            {:keys [status data error]} query]
        [:div {:on-click (fn [_] (rf/dispatch [::rq/mutate foo-mutation]))}
         [:div (case status
                 (:loading nil) "Loading"
                 :error (str "Error: " (.-message error))
                 :success (str "Count: " data))]
         [:pre [:code (with-out-str (pprint/pprint query))]]]))))

(defn app []
  [queries/provider {:mutations [foo-mutation]}
   [hello]])

(def container (js/document.getElementById "app"))

(def root (rdom/create-root container))

(defn -main [& _]
  (rdom/render root [app]))
