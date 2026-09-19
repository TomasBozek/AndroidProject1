# Release G · A real API

Status: open
Sprints:
Decisions pre-assigned: D79–D81

The release that gives the sample a list built from data nobody in this repository wrote. Every
list so far reads a fixture the `dev` flavor answers from `res/raw` (D20), loads whole, and refreshes
because a screen opened — nothing pages, nothing pulls to refresh, and no build can be pointed at a
server that exists. Release G adds a feature over a public API — TMDB, the owner's choice on
2026-09-19 — with the four things the catalog does not have: pages loaded as the list scrolls, a
pull to refresh, a cache the list reads when the network is gone, and a switch on the dev menu
that sends the fixture build to the real host. The first sprint is that feature; what the sprints
after it take is the owner's call at each close. Ships as `v1.4.0` with `/release close`.
