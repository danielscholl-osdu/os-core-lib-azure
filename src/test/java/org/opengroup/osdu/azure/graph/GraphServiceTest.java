package org.opengroup.osdu.azure.graph;

import com.microsoft.graph.models.User;
import com.microsoft.graph.models.odataerrors.ODataError;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.graph.users.UsersRequestBuilder;
import com.microsoft.graph.users.item.UserItemRequestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.http.AppException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GraphServiceTest {

    @InjectMocks
    GraphService sut;

    @Mock
    private GraphServiceClientFactory graphServiceClientFactory;

    @Mock
    private GraphServiceClient graphServiceClient;

    @Test
    void getByOidReturnsUserId_ifTokenIssuerIsAADAndOIDIsValid() {
        User userInfo = new User();
        userInfo.setGivenName("userName");
        UserItemRequestBuilder response = mock(UserItemRequestBuilder.class);

        when(response.get()).thenReturn(userInfo);
        when(graphServiceClientFactory.getGraphServiceClient("data-partition")).thenReturn(graphServiceClient);
        UsersRequestBuilder usersRequestBuilder = mock(UsersRequestBuilder.class);
        when(usersRequestBuilder.byUserId("oid")).thenReturn(response);

        when(graphServiceClient.users()).thenReturn(usersRequestBuilder);

        assertEquals("userName", sut.getByOid("data-partition", "oid"));
    }

    @Test
    void getByOidThrowsAppException_ifDataPartitionIsEmpty() {
        assertThrows(AppException.class,
                () -> {
                    sut.getByOid("", "oid");
                });
    }

    @Test
    void getByOidThrowsAppException_ifOidIsEmpty() {
        assertThrows(AppException.class,
                () -> {
                    sut.getByOid("data-partition", "");
                });
    }

    @Test
    void getByOidThrowsAppException_ifGraphClientThrowsNotFoundODataError() {
        when(graphServiceClientFactory.getGraphServiceClient("data-partition")).thenReturn(graphServiceClient);

        ODataError error = mock(ODataError.class);
        when(error.getMessage()).thenReturn("Not Found");
        when(error.getResponseStatusCode()).thenReturn(404);

        UsersRequestBuilder usersRequestBuilder = mock(UsersRequestBuilder.class);
        when(usersRequestBuilder.byUserId("oid")).thenThrow(error);

        when(graphServiceClient.users()).thenReturn(usersRequestBuilder);


        AppException exception = assertThrows(AppException.class,
                () -> {
                    sut.getByOid("data-partition", "oid");
                });

        assertEquals(error.getMessage(), exception.getMessage());
        assertEquals(error.getResponseStatusCode(), exception.getError().getCode());

    }

    @Test
    void getByOidThrowsAppException_ifGraphClientThrowsAnyOtherODataErrorFromUsersAPI() {
        when(graphServiceClientFactory.getGraphServiceClient("data-partition")).thenReturn(graphServiceClient);

        ODataError error = mock(ODataError.class);
        when(error.getMessage()).thenReturn("BadRequest");
        when(error.getResponseStatusCode()).thenReturn(400);

        when(graphServiceClient.users()).thenThrow(error);


        AppException exception = assertThrows(AppException.class,
                () -> {
                    sut.getByOid("data-partition", "oid");
                });

        assertEquals(error.getMessage(), exception.getMessage());
        assertEquals(error.getResponseStatusCode(), exception.getError().getCode());

    }
}