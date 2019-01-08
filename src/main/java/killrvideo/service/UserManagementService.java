package killrvideo.service;

import java.util.UUID;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.grpc.stub.StreamObserver;
import killrvideo.user_management.UserManagementServiceGrpc.UserManagementServiceImplBase;
import killrvideo.user_management.UserManagementServiceOuterClass.CreateUserRequest;
import killrvideo.user_management.UserManagementServiceOuterClass.CreateUserResponse;
import killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileRequest;
import killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileResponse;
import killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsRequest;
import killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsResponse;
import killrvideo.utils.TypeConverter;
import killrvideo.validation.KillrVideoInputValidator;
import killrvideo.common.CommonTypes.Uuid;
import killrvideo.dataLayer.UserAccess;
import killrvideo.entity.User;
import killrvideo.common.CommonTypes;


@Service
//public class UserManagementService extends AbstractUserManagementService {
public class UserManagementService extends UserManagementServiceImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementService.class);
    private UserAccess userAccess = new UserAccess();

    @Inject
    KillrVideoInputValidator validator;
   
    @PostConstruct
    public void init(){
    }

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<CreateUserResponse> responseObserver) {

        LOGGER.debug("-----Start creating user-----");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        try
        {        
            CommonTypes.Uuid userIdUuid = request.getUserId();
            UUID userIdUUID = UUID.fromString(userIdUuid.getValue());
            String firstName = request.getFirstName();
            String lastName = request.getLastName();
            
            String password = request.getPassword();
            String email = request.getEmail();
            
            User user = new User();
            user.setUserid(userIdUUID);
            user.setFirstname(firstName);
            user.setLastname(lastName);
            user.setEmail(email);
            
            UUID userId = userAccess.createNewUser(password, user);   
            
            if (userId == null){
                LOGGER.info("User already exists");
                responseObserver.onError(new Throwable("User already exists"));
            } else {
                    LOGGER.info("Boom shakalaka 'new toys!'");
                    LOGGER.info("User ID: " + userId);
                    
                    responseObserver.onNext(CreateUserResponse.newBuilder().build());
                    responseObserver.onCompleted();
                    LOGGER.info("Response complete.");
            }
        }
        catch(Throwable e)
        {
            e.printStackTrace();
            LOGGER.debug("Error: " + e);
        }

    }

    @Override
    public void verifyCredentials(VerifyCredentialsRequest request, StreamObserver<VerifyCredentialsResponse> responseObserver) {

       LOGGER.debug("------Start verifying user credentials------");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        try
        {        
            String email = request.getEmail();
            String passwordFromRequest = request.getPassword();
            UUID userId = userAccess.getAuthenticatedIdByEmailPassword(email, passwordFromRequest);
            
            if (userId == null){
                LOGGER.info("Invalid credentials");
                responseObserver.onError(new Throwable("Invalid credentials"));
            } else {
                    LOGGER.info("Boom shakalaka we in!");
                    LOGGER.info("User ID: " + userId);
                    
                    responseObserver.onNext(VerifyCredentialsResponse
                                .newBuilder()
                                .setUserId(TypeConverter.uuidToUuid(userId))
                                .build());
                    responseObserver.onCompleted();
                    LOGGER.info("Response complete.");
            }
        }
        catch(Throwable e)
        {
            e.printStackTrace();
            LOGGER.debug("Error: " + e);
        }
       
    }


    @Override
    public void getUserProfile(GetUserProfileRequest request, StreamObserver<GetUserProfileResponse> responseObserver) {

        LOGGER.debug("------Start getting user profile------");

        if (!validator.isValid(request, responseObserver)) {
            return;
        }

        try {

            Uuid id = request.getUserIds(0);
            UUID userid = UUID.fromString(id.getValue());
            LOGGER.debug("userid = "+userid.toString());
            User user = userAccess.getUserById(userid);

            final GetUserProfileResponse.Builder builder = GetUserProfileResponse.newBuilder();
    
            builder.addProfiles(user.toUserProfile());
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        }
        catch (Exception e) {e.printStackTrace();}
    }
}