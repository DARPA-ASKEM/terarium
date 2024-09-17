## Search and Sort
Please go through __every__ step of the test scenario.\
Report any issues into GitHub: [open an issue](https://github.com/DARPA-ASKEM/terarium/issues/new?assignees=&labels=bug%2C+Q%26A&template=qa-issue.md&title=%5BBUG%5D%3A+).

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create project named `(2) QA [Your Name]`
   - The Description should be `Bees`
   - return to home page by clicking on logo in the top left
3. Create project named `(3) QA [Your Name]`
   - The Description should be `Wings`
   - return to home page by clicking on logo in the top left
4. Create project named `(1) QA [Your Name]`
   - The Description should be `Cars`
   - return to home page by clicking on logo in the top left

### 2. Search for Project By Name
1. Type in the number (3) into the project search field
2. project (3) plus any other project with that value should only be showing
3. clear the search field
4. Enter `[Your Name]` into the search field

### 3. Sort Projects
1. In the drop the menu next to the search field select `Alphabetical`
   - The 3 projects created should be in listed in ascending order base off of their name `(1, 2, 3)`
2. Select `Creation Date (ascending)`.
   - The 3 projects should be in the order `(2, 3, 1)`
3. Select `Creation Date (descending)`.
   - The 3 projects should be in the order `(1, 3, 2)`
4. Open the project card menu for project (3) and select `Edit project details` for project (3)
   - change the description to `Apples`
5. Select `Last updated (ascending)`
   - The 3 projects should be in the order `(2, 1, 3)`
6. Select `Last updated (descending)`
   - The 3 projects should be in the order `(3, 1, 2)`

### 4. Share your project
1. Open another browser and login with another account
2. On a project card one the menu and select `share`
3. Share with the `Quality Assurance` account
4. Return to the browser with the `Quality Assurance` account open

### 5. My Projects Table View Sort
1. Click on the table toggle next to the new project button
2. Repeat Step `2. Search for Project By Name`
3. Click the `Project Title` a few times keeping an on the 3 projects order
   - the order should switch between `(1,2,3) & (3,2,1)`
4. Click the `Descripton` a few times keeping an on the 3 projects order
   - the order should switch between `(3 Apples, 2 Bees, 1 Cars) & (1 Cars, 2 Bees, 3 Apples)`
5. Click the `Author` a few times keeping an on the 3 projects order
   - the order will switch between all the `Quality Assurance` projects being first and the other account project
6. Click the `Create on` a few times keeping an on the 3 projects order
   - the order should switch between `(2,3,1) & (1,3,2)`
7. Click the `Last updated` a few times keeping an on the 3 projects order
   - the order should switch between `(3,1,2) & (2,1,3)`
