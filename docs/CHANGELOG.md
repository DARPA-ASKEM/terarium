# Change log

A log of _Terarium_ infrastructure to records all the changes made to the technology stack. 
The changes may include updates or upgrades to programming languages, tools, libraries, or frameworks. 

## Goal
The document is essential for post-mortem analysis, where it helps identify the root cause of critical issues that may arise and inform future infrastructure choices. 
It provides a detailed account of the changes made to the infrastructure, including the reasons for the changes, the dates of implementation, and the benefits or impact of the changes. 
This information can be used to assess the effectiveness of the changes and inform future decisions.


## Log

### 2023 April
 - Front-end:
 Added [event-source-polyfill](https://github.com/Yaffle/EventSource) to handle Server Side Events (SSE) with the use of a Bearer tokens in the header.
 
 - Back-end: 
 Upgrading _Quarkus_ to the latest version `3.0` and using their default choice of Reactive library with _SmallRye Reactive_ as REST.

### Kickoff
  - Network: _kubernetes_, _Docker Desktop_ locally, and _AWS_ for `staging` and `production`
  - Back-end: _Java_, using _Quarkus_ framework, and _SmallRye Rest_ as REST library
  - Front-end: _TypeScript_, using _Vite.js, eslint, and Prettier_, with _Axios_ as REST library 
