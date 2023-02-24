(ns rf-query.example.main
  (:require [cljs.pprint :as pprint]
            [reagent.dom :as rdom]
            [rf-query.core :as rq]
            [rf-query.example.queries :as queries]))

(def container (js/document.getElementById "app"))

(defn hello [_]
  (let [counter @(rq/subscribe queries/counter)]
    [:pre [:code (with-out-str (pprint/pprint counter))]]))

(defn app []
  [queries/provider
   [:f> hello]])

(defn -main [& _]
  (rdom/render [app] container))
