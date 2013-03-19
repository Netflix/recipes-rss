RSS Recipes
===========

RSS is a Netflix Recipes application demonstrating how all of the following Netflix Open Source components can be tied together.

* [archaius] (https://github.com/Netflix/archaius)
* [astyanax] (https://github.com/Netflix/astyanax)
* [blitz4j] (https://github.com/Netflix/blitz4j)
* [eureka] (https://github.com/Netflix/eureka)
* [governator] (https://github.com/Netflix/governator)
* [hystrix] (https://github.com/Netflix/hystrix)
* [karyon] (https://github.com/Netflix/karyon)
* [ribbon] (https://github.com/Netflix/ribbon)
* [servo] (https://github.com/Netflix/servo)

Modules
=======

rss-core
-----------
Shared classes between edge and middletier.

rss-edge
-----------
Customer-facing edge service. The RSS Reader UI is hosted in this server.

rss-middletier
-----------------
Internal middletier service responsible for fetching RSS feeds and for persisting user subscriptions.

Documentation
--------------
Please see [wiki] (https://github.com/Netflix/recipes-rss/wiki) for detailed documentation.

Communication
--------------
* Google Group: [netflix-oss-recipe] (https://groups.google.com/forum/#!forum/netflix-oss-recipe)
* [GitHub Issues] (https://github.com/Netflix/recipes-rss/issues)
