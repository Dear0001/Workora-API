## E Certify

### Getting Started
#### Installation
> NOTE:
<br>
> * JDK 21 required for all installation methods
    <br>
> * Install Postgresql
    <br>
> * Create database name bugzapper_db
    <br>
> * Run All SQl statement in script folder

## Usage
### Steps to test API


### [AuthController](http://localhost:8080/swagger-ui/index.html#/auth-controller)

> Step 01: <br>
You need to register with local-register or google-auth
    
- [Register bug hunter by local register](http://localhost:8080/swagger-ui/index.html#/auth-controller/local-register)

- [Register bug hunter by local register](http://localhost:8080/swagger-ui/index.html#/auth-controller/google-auth)
<br>

> Step 02: <br>
Users need to verify email address by code that within in gmail account

- [User verify email](http://localhost:8080/swagger-ui/index.html#/auth-controller/verify)

<br>

> Step 03: <br>
Users login account

- [Login](http://localhost:8080/swagger-ui/index.html#/auth-controller/local-login)

<br>

> Step 04: <br>
User forgot password by provide their email address and then code will send to their email, (no need to log in account)

- [Forgot Password](http://localhost:8080/swagger-ui/index.html#/auth-controller/change-password)

<br>

> Step 05: <br>
Users reset password by get code from their email in <b> step 04 </b> to confirm

- [Reset Password](http://localhost:8080/swagger-ui/index.html#/auth-controller/reset)

<br>

### [UserController](http://localhost:8080/swagger-ui/index.html#/user-controller) 

> Step 01: <br>
Update user

- [Update User](http://localhost:8080/swagger-ui/index.html#/user-controller/updateUser)

<br>

> Step 02: <br>
User can insert his experience

- [Insert User Experience](http://localhost:8080/swagger-ui/index.html#/user-controller/insertUserExperience)

<br>

> Step 03: <br>
Get user details

- [Get Use By Id](http://localhost:8080/swagger-ui/index.html#/user-controller/getUserById)

<br>

> Step 04: <br>
Delete user

- [Delete User](http://localhost:8080/swagger-ui/index.html#/user-controller/DeleteUser)

<br>

> Step 05: <br>
get history of user

- [Get History](http://localhost:8080/swagger-ui/index.html#/user-controller/history)

<br>

### [CompanyController](http://localhost:8080/swagger-ui/index.html#/company-controller) 

> Step 01: <br>
Users can create company

- [Create Company](http://localhost:8080/swagger-ui/index.html#/company-controller/CreateCompany)

<br>

> Step 02: <br>
Only admin can update company

- [Update Company](http://localhost:8080/swagger-ui/index.html#/company-controller/UpdateCompany)

<br>

> Step 03: <br>
Only admin can delete company

- [Delete Company](http://localhost:8080/swagger-ui/index.html#/company-controller/DeleteCompany)

<br>

> Step 04: <br>
Only admin get company by ID, use when admin need to update

- [Get Company By ID](http://localhost:8080/swagger-ui/index.html#/company-controller/GetCompanyById)



<br>


### [ProjectController](http://localhost:8080/swagger-ui/index.html#/project-controller)

> Step 01: <br>
Only admin can create project

- [Create Project](http://localhost:8080/swagger-ui/index.html#/project-controller/CreateProject)

<br>

> Step 02: <br>
Admin and project manager can update project

- [Update Project](http://localhost:8080/swagger-ui/index.html#/project-controller/UpdateProject)


<br>

> Step 03: <br>
Only admin can delete project

- [Delete Project](http://localhost:8080/swagger-ui/index.html#/project-controller/DeleteProject)


<br>

> Step 04: <br>
Admin and project manager can get project by ID, use when admin or project manager need to update

- [Get Project By ID](http://localhost:8080/swagger-ui/index.html#/project-controller/GetProjectById)


<br>

> Step 06: <br>
Only admin can get all project

- [Get All Phase](http://localhost:8080/swagger-ui/index.html#/project-controller/GetAllPhase)

<br>

### [PhaseController](http://localhost:8080/swagger-ui/index.html#/phase-controller)

> Step 01: <br>
Admin and project manager can create phase

- [Create Phase](http://localhost:8080/swagger-ui/index.html#/phase-controller/CreatePhase)

<br>

> Step 02: <br>
Admin and project manager can update phase

- [Update Phase](http://localhost:8080/swagger-ui/index.html#/phase-controller/UpdatePhase)


<br>

> Step 03: <br>
Admin and project manager can delete phase

- [Delete Phase](http://localhost:8080/swagger-ui/index.html#/phase-controller/DeletePhase)


<br>

> Step 04: <br>
Admin and project manager can get phase by ID, use when admin or project manager need to update

- [Get Phase By ID](http://localhost:8080/swagger-ui/index.html#/phase-controller/GetPhaseById)


<br>

> Step 06: <br>
Admin and project manager can get all phases (public and private) and normal user can only see public phases

- [Get All Phase](http://localhost:8080/swagger-ui/index.html#/phase-controller/GetAllPhase)

<br>

> Step 07: <br>
Admin and project manager can add members to phases 

- [Add Members](http://localhost:8080/swagger-ui/index.html#/phase-controller/AddMemberToPhase)

<br>

### [TaskController](http://localhost:8080/swagger-ui/index.html#/task-controller)

> Step 01: <br>
Admin and project manager can create task

- [Create Task](http://localhost:8080/swagger-ui/index.html#/task-controller/CreateTask)

<br>

> Step 02: <br>
Admin and project manager can update task

- [Update Task](http://localhost:8080/swagger-ui/index.html#/task-controller/UpdateTask)


<br>

> Step 03: <br>
Admin and project manager can delete task

- [Delete Task](http://localhost:8080/swagger-ui/index.html#/task-controller/DeleteTask)


<br>

> Step 04: <br>
Admin and project manager can get task by ID, use when admin or project manager need to update

- [Get Task By ID](http://localhost:8080/swagger-ui/index.html#/task-controller/GetTaskById)


<br>

> Step 06: <br>
Admin and project manager can get all tasks 

- [Get All Task](http://localhost:8080/swagger-ui/index.html#/task-controller/GetAllTask)

<br>

> Step 07: <br>
Admin and project manager can assign task to members

- [Assign task to Members](http://localhost:8080/swagger-ui/index.html#/task-controller/AssignTaskToMemberToCompany)

<br>

> Step 08: <br>
Admin and project manager can get all task that has submitted

- [All task that submitted](http://localhost:8080/swagger-ui/index.html#/task-controller/GetAllSubmitDataByTaskId)

<br>

> Step 09: <br>
Task member can update task status

- [Update task status](http://localhost:8080/swagger-ui/index.html#/task-controller/UpdateTaskStatus)

<br>

> Step 10: <br>
Task member can submit task

- [Submit task](http://localhost:8080/swagger-ui/index.html#/task-controller/SubmitTask)

<br>

### [RateFeedbackController](http://localhost:8080/swagger-ui/index.html#/ratefeedback-controller)

> Step 01: <br>
Users can rate and feedback to companies

- [User rate feedback Company ](http://localhost:8080/swagger-ui/index.html#/ratefeedback-controller/UserRateFeedbackToCompany)

<br>

> Step 02: <br>
Companies can rate and feedback to users

- [Company rate feedback user](http://localhost:8080/swagger-ui/index.html#/ratefeedback-controller/CompanyRateFeedbackUser)

<br>

> Step 03: <br>
User can get all his rate and feedback that companies rated to him

- [Get all rate feedback of user](http://localhost:8080/swagger-ui/index.html#/ratefeedback-controller/GetAllRateFeedbackOfUser)

<br>

> Step 04: <br>
Company can get all his rate and feedback that users rated to 

- [Get all rate feedback of company](http://localhost:8080/swagger-ui/index.html#/ratefeedback-controller/GetAllRateFeedbackOfCompany)

<br>

### [PostRecruitmentController](http://localhost:8080/swagger-ui/index.html#/postrecrutment-controller)

> Step 01: <br>
Only admin can post recruitment post

- [Post Recruitment post](http://localhost:8080/swagger-ui/index.html#/postrecrutment-controller/createPostRecruitment)

<br>

> Step 02: <br>
Only admin can update recruitment post

- [Update Recruitment Post](http://localhost:8080/swagger-ui/index.html#/postrecruitment-controller/updatePostRecruitmentById)

<br>

> Step 03: <br>
All user role can get all recruitment posts

- [Get All Recruitment Post](http://localhost:8080/swagger-ui/index.html#/postrecruitment-controller/GetAllPostRecruitment)

<br>

> Step 04: <br>
All user role can view recruitment post details

- [Get Recruitment post details](http://localhost:8080/swagger-ui/index.html#/postrecruitment-controller/GetPostRecruitmentById)

<br>

> Step 05: <br>
Only admin can delete post recruitment

- [Delete Post Recruitment](http://localhost:8080/swagger-ui/index.html#/postrecruitment-controller/deletePostRecruitmentById)

<br>

### [PhaseAttachmentController](http://localhost:8080/swagger-ui/index.html#/phaseAttachment-controller)

> Step 01: <br>
Admin and project manager can create phase attachment

- [Create Phase Attachment](http://localhost:8080/swagger-ui/index.html#/phaseAttachment-controller/CreatePhaseAttachment)

<br>

> Step 02: <br>
Admin and project manager can uppdate phase attachment

- [Update Phase Attachment](http://localhost:8080/swagger-ui/index.html#/phaseAttachment-controller/UpdatePhaseAttachmentById)

<br>

> Step 03: <br>
Admin and project manager can view phase attachment details

- [Get Phase Attachment](http://localhost:8080/swagger-ui/index.html#/phaseAttachment-controller/GetPhaseAttachmentById)

<br>

> Step 04: <br>
Admin and project manager can delete phase attachment

- [Get Phase Attachment](http://localhost:8080/swagger-ui/index.html#/phaseAttachment-controller/GetPhaseAttachmentById)

<br>

### [DashboardController](http://localhost:8080/swagger-ui/index.html#/dashboard-controller)

> Step 01: <br>
Current user can get all task that was assigned to him

- [Get All Tasks](http://localhost:8080/swagger-ui/index.html#/dashboard-controller/getAllTasks)

<br>

> Step 02: <br>
Current user can get all reports that bug hunters report to his companies

- [Get Notification By User](http://localhost:8080/swagger-ui/index.html#/dashboard-controller/GetAllNotificationsByUser)

<br>

> Step 03: <br>
Update notification, check read or unread

- [Update notification status by id](http://localhost:8080/swagger-ui/index.html#/dashboard-controller/UpdateNotificationStatusById)

<br>

> Step 04: <br>
Current user can get all project that he join

- [Get All Project](http://localhost:8080/swagger-ui/index.html#/dashboard-controller/GetAllProjects)

<br>

> Step 05: <br>
Current user can get all his own companies

- [Get All Own Company](http://localhost:8080/swagger-ui/index.html#/dashboard-controller/GetAllOwnCompany)

<br>

> Step 06: <br>
Current user can get all his notification

- [Get All Notification](http://localhost:8080/swagger-ui/index.html#/dashboard-controller/GetAllNotification)

<br>

> Step 07: <br>
Current user can get all amount of company that he joined, project that he joined, task that he in and bug hunter report 

- [Get All Count](http://localhost:8080/swagger-ui/index.html#/dashboard-controller/GetAllCount)

<br>

> Step 08: <br>
Current user can get all companies that he joined

- [Get All Companies](http://localhost:8080/swagger-ui/index.html#/dashboard-controller/GetAllCompanies)

<br>

> Step 09: <br>
Current user can get all application that bug hunter apply to his companies

- [Get All Application](http://localhost:8080/swagger-ui/index.html#/dashboard-controller/GetAllApplies)

<br>

> Step 10: <br>
Admin, project manager and phase members can see amount of task status

- [Count Phase Status By Phase Id](http://localhost:8080/swagger-ui/index.html#/dashboard-controller/CountPhaseStatusById)

<br>

### [ApplyController](http://localhost:8080/swagger-ui/index.html#/apply-controller)

> Step 01: <br>
Bug hunter can apply on post recruitment

- [Create Apply](http://localhost:8080/swagger-ui/index.html#/apply-controller/CreateApply)

<br>

> Step 02: <br>
Only admin can see apply details

- [Get Apply By User](http://localhost:8080/swagger-ui/index.html#/apply-controller/GetApplyById)

<br>

### [NotificationController](http://localhost:8080/swagger-ui/index.html#/notification-controller)

> Step 01: <br>
Current user can get all his notification

- [Get All Notification](http://localhost:8080/swagger-ui/index.html#/notification-controller/GetAllNotification)

<br>

> Step 02: <br>
Current user can delete his notification

- [Delete Notification](http://localhost:8080/swagger-ui/index.html#/notification-controller/deleteNotificationById)

<br>

### [ReportController](http://localhost:8080/swagger-ui/index.html#/report-controller)

> Step 01: <br>
Bug hunter can report to publice phase

- [Create Report Phase](http://localhost:8080/swagger-ui/index.html#/report-controller/CreateReportPhase)

<br>

> Step 02: <br>
Admin and project manager can see report details

- [Get Report By Id](http://localhost:8080/swagger-ui/index.html#/report-controller/getReportById)

<br>

### [OneSignalPushNotificationController](http://localhost:8080/swagger-ui/index.html#/one-signal-push-notification-controller)

> Step 01: <br>
Push notification

- [Send Message To User](http://localhost:8080/swagger-ui/index.html#/one-signal-push-notification-controller/sendMessageToUser)

<br>

### [NewfeedController](http://localhost:8080/swagger-ui/index.html#/newfeed-controller)

> Step 01: <br>
Get all post to new feed

- [Get New Feed](http://localhost:8080/swagger-ui/index.html#/newfeed-controller/getNewfeed)

<br>

### [Image-S-3-Controller"](http://localhost:8080/swagger-ui/index.html#/image-s-3-controller)

> Step 01: <br>
Upload user profile

- [Upload Profile](http://localhost:8080/swagger-ui/index.html#/image-s-3-controller/uploadUserProfile)

<br>

> Step 02: <br>
Upload File

- [Upload File](http://localhost:8080/swagger-ui/index.html#/image-controller/uploadFile)

<br>

> Step 03: <br>
Upload company profile

- [Upload Company Profile](http://localhost:8080/swagger-ui/index.html#/image-s-3-controller/uploadCompanyProfile)

<br>

> Step 04: <br>
view file

- [View File](http://localhost:8080/swagger-ui/index.html#/image-s-3-controller/view)

<br>

> Step 05: <br>
download file

- [Download File](http://localhost:8080/swagger-ui/index.html#/image-s-3-controller/download)

<br>

> Step 05: <br>
delete file

- [Delete File](http://localhost:8080/swagger-ui/index.html#/image-s-3-controller/delete)
