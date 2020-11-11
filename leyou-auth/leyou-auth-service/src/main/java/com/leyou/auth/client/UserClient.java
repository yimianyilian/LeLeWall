package com.leyou.auth.client;

import com.leyou.userservice.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "user-service")
public interface UserClient extends UserApi {

}
