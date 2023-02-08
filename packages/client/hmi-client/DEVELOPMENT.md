# Development hygiene

Basic rules to write organised code.

## Folder organisation

`page`
: list of folders matching the current pages of terarium.

`components`
: only components used everywhere in terarium.
: or base components, which are extended in each `page`.

`services`*
: domain specific to terarium; where calls to API are made; good place to unload big functions off a component.

`utils`*
: generic functions; utilities that can be reused easily.

Folders with an asterisk (*), every function within should have their own test.

## Naming convention

Default
: All names should be `kebab-case`.

Events
: context followed by the action
: _i.e._ `query-updated` or `document-added-to-project`

Types
: types are in `PascalCase` and end with `Type`
: please use `interface` instead of `type` to define them
: _i.e._ `ModelType` or `DocumentNameType`

## Components

* Component name are preceded by `tera`

    _.i.e_ `/components/slider.vue` -> `<tera-slider>` 

* Vue single-file architecture order is: 
    ```html
    <template></template>
    <script lang=ts></script>
    <style scoped></style>
    ```

* Leverage HTML5 semantic tag to improve lisibility instead of `<div>` soup and weirdly named classnames.
    ```html
    <template>
        <main>                      <!-- <div class="component-container"> -->
            <header></header>           <!-- <div class="header"></div> -->
            <section></section>         <!-- <div></div> -->
            <section></section>         <!-- <div></div> -->
            <aside></aside>             <!-- <div class="sidebar"></div> -->
            <footer></footer>           <!-- <div class="footer"></div> -->
        </main>                     <!-- </div> -->
    </template>

    <style scoped>
        main {}                     /* .component-container {} */
        section {}                  /* .component-container > div {} */
        aside {}                    /* .sidebar {} */
    </style>
    ```
