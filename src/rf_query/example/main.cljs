(ns rf-query.example.main
  (:require [cljs.pprint :as pprint]
            [re-frame.core :as rf]
            [reagent.dom :as rdom]
            [rf-query.core :as rq]
            [rf-query.example.queries :as queries]))

(def hello
  (rq/with-queries
    [queries/counter]
    (fn [_]
      (let [query @(rf/subscribe [::rq/query-state queries/counter])
            {:keys [status data error]} query]
        [:div
         [:div (case status
                 :loading "Loading"
                 :error (str "Error: " (.-message error))
                 :success (str "Count: " data))]
         [:pre [:code (with-out-str (pprint/pprint query))]]]))))

(defn app []
  [queries/provider
   [hello]])

(def container (js/document.getElementById "app"))

(defn -main [& _]
  (rdom/render [app] container))
