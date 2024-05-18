# ProxyAuth: Where accuracy meets innovation in attendance tracking.

In today's work environment, monitoring employee attendance is crucial for effective
organizational management. An Employee Tracking System in general is a software-based
solution designed to help organizations efficiently monitor and manage their employees'
activities, tasks, and performance within the workplace. This system encompasses a range of
functionalities and features aimed at enhancing productivity, ensuring compliance, and
promoting transparency.

The major challenge in managing attendance is the issue of proxy attendance. Traditional
methods of attendance tracking like manual sign-ins or punch cards are prone to manipulation,
with employees marking attendance on behalf of absent colleagues. This leads to inaccuracies in
attendance records, which can have significant consequences like overpayment or underpayment
of employees, affect performance assessments, and reduce organizational efficiency.

In this project, we proposed a solution to address the problem of proxy attendance and the
inaccuracies in attendance by using a GPS-enabled attendance tracking application that
incorporates image verification through facial recognition technology. During attendance
recording, employees are required to take a photo, and facial recognition algorithm verifies their
identification. GPS records their whereabouts to verify they are at work throughout this period.
By combining GPS technology and facial recognition, organizations can expect a more efficient
and secure solution for monitoring employee attendance, reducing the occurrence of proxy
attendance, and enhancing the accuracy and reliability of attendance records.

## [Demo Link](https://drive.google.com/file/d/1MalGSODvQtrU_xNe8AcwdnapLY4P0T9B/view?usp=drive_link)

## [Pitch](https://drive.google.com/file/d/1KnudiRGRkNyzNAls6q3noO6vvehvPFe1/view?usp=drive_link)

## To rebuild the project:
### Frontend (Android)
1. Clone the repo
2. Run the project on Android studios
3. create an emulator with a min of 33 API
4. Add backend URL to [RetrofitClient.java](src/main/java/com/example/checking/Service/RetrofitClient.java)

### Backend (Spring Boot)
1. clone the [repo](https://github.com/chisunku/proxyAuthAPIs.git)
2. add mongodb url and DB name to application.properties
3. run the backend

### DB
1. create a mongoDB cluster
2. fetch the mongoDB URI
3. insert it into backend applications.properties

Note: All the documents models are inside the code and will get created when you run the backend. 
