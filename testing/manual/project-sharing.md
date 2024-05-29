## Sharing a Project
Please go through __every__ step of the test scenario.\
When blocked, an error, or a UI/UX anomaly occurs, please report which scenario and step to [\#askem-testing](https://unchartedsoftware.slack.com/archives/C06FGLXB2CE).

Estimated time to completion: [X] minutes

### 1. Begin test
1. Login to https://app.staging.terarium.ai using the test account
    ```
    email: qa@test.io
    password: askem-quality-assurance
    ```
2. Create, project named `QA [Your Name]`

### 2. Making Project Public
1. Return to the home page by clicking on the Terarium Logo in the top left.
2. Then select your dropdown menu on your project card (should be 3 dots).
3. Select the share option.
4. A Share Project modal should appear.
   - Select Public from General Access
   - click Done
5. The Project Card should appear under the Public Projects.

### 3. Sharing the Project as Read Only
1. Open the Sharing Modal again.
2. Enter your account name and select it from the dropdown list of users.
3. Change your the Access to `Edit`.
4. Change the General Access to `Restricted`

### 4. See if your account can see Q&A Project
1. Open a browser incognito mode or another browser.
2. Login to https://app.staging.terarium.ai using your account.
3. The Project Card should appear your account's My Projects.
4. Open the Project Card Sub menu you should be able to edit the project from the card menu.
   - Change the description to 'Editing project'
5. Open the Project.
   - Change the description to 'done editing'

### 5. End test
1. Logout of the application for both accounts.
