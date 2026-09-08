---
description: Scaffold a data source across gateway/data/di, optionally with its repository
argument-hint: <feature> <LocalXName> [--repository]
allowed-tools: Bash(python3 scripts/*), Read, Edit, Glob, Grep
---

```bash
python3 scripts/create_datasource.py $ARGUMENTS
```

The point is the layer inversion, which is the thing most often got backwards by hand: the
`XDataSource` **interface** belongs to `gateway`, `DefaultXDataSource` to `data`. So `data` depends
on `gateway`, not the reverse. `--repository` adds `XRepository` in `domain` and
`DefaultXRepository` in `gateway`.

Then:

1. Replace the placeholder `observeValue` / `setValue` with the real operations, in the interface,
   the implementation, and the repository pair.
2. The generated implementation is DataStore-backed so it compiles. Swap it for the real source and
   leave the interface where it is.
3. **Switch dispatcher in the data source, not the repository** — `BaseRepository` runs on the
   caller's context, and the caller is `viewModelScope`, which is `Dispatchers.Main`. Inject
   `DispatcherProvider` and wrap the blocking call in `withContext(dispatcherProvider.io)`, as
   `DefaultLocalAuthDataSource` does.
4. Repository methods return `Outcome` and go through `execute` / `observe`. Pass `retries` when the
   collector outlives a failure.
