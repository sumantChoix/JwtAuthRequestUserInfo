package com.order.controller;

import com.order.Entity.UserCredential;
import com.order.Repo.UserCredentialRepo;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import com.order.ControllerService.UserInfoService;
import com.order.Entity.AuthEvent;
import com.order.Entity.UserInfo;
import com.order.JwtService.JwtUtil;
import com.order.Repo.UserInfoRepo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@RestController
@CrossOrigin
public class FrontController {
	
	
	    @Autowired
	    private JwtUtil jwtUtil;
	    @Autowired
	    private AuthenticationManager authenticationManager;
	    @Autowired
	    private UserInfoService ser;
	    @Autowired
		private UserInfoRepo userInfoRepo;
	   @Autowired
	    private UserCredentialRepo userCredentialRepo;



	@GetMapping("/userId/{userIdentifier}")
	public UserInfo getCustomerById(@PathVariable("userIdentifier") String userIdentifier){
		System.out.println(userIdentifier);
		return ser.userFindByPhone(userIdentifier);
	}

	@GetMapping("/")
	public String home() {
		return "Hello world";
	}
	@GetMapping("/hello/{userName}")
	public String CreateUserInfo(@PathVariable("userName") String userName) {
		boolean present=ser.ifUserPresent(userName);
		String  user = "present";
		if(!present) {
			user="absent";
		}
		return user;
		
	}
	
	@PostMapping("/authenticate/{userType}/{userIdentifier}")
	public JSONObject AcessInApplication(@PathVariable("userType") String userType ,
			@PathVariable("userIdentifier") String userIdentifier
			,@RequestBody AuthRequest authRequest) throws Exception {
		String token=ser.generateToken(authRequest);
		String text;
		System.out.println("in controller");


		System.out.println("UserService");
		if(ser.ifUserPresent(authRequest.getUserName())){
			UserInfo userInfo=new UserInfo();
			userInfo.setUserName(authRequest.getUserName());
			String EntryTimeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
			userInfo.setCreatedAt(EntryTimeStamp);
			userInfo.setUpdatedAt(null);
			userInfo.setUserType(userType);
			userInfo.setPassword(null);
			ser.createUserInfoService(userInfo);

		}else {

			text = "user already present";
		}


		long userinfo=ser.FindIdByUsingPhone(authRequest.getUserName());
		System.out.println(userinfo);


		AuthEvent authEvent=new AuthEvent();
        authEvent.setUserInfoId(userinfo);
        authEvent.setToken(token);
        authEvent.setUuid(authRequest.getUuid());
        authEvent.setUuid(authEvent.getUuid());
		String loginTimeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        authEvent.setLoginAt(loginTimeStamp);
		InetAddress ip;
		String hostname = null;
		try {
			ip = InetAddress.getLocalHost();
			hostname = ip.getHostName();
		} catch (UnknownHostException e) {

			e.printStackTrace();
		}
		authEvent.setIpAddress(hostname);
		authEvent.setUserAgent(userIdentifier);

        ser.CreateAuthEventService(authEvent);


		JSONObject jsonObject = new JSONObject();
		jsonObject.put("jwtToken", token);
		jsonObject.put("UserName", authRequest.getUserName());
		jsonObject.put("UUID",authRequest.getUuid());
		return jsonObject;
	}

	@PostMapping("/authenticate/inputUser")
	public UserCredential createUserCredential(@RequestBody UserCredential userCredential)
	{
		return userCredentialRepo.save(userCredential);
	}
	@GetMapping("/authenticate/authEvent")
	public List<AuthEvent> findAll(){
		return ser.findAllInAuthEvent();
	}
	
	@GetMapping("/authenticate/userInfo")
	public List<UserInfo> findAllUserInfo(){
		return ser.findAllinUserInfo();
	}
	
	@GetMapping("/authenticate/inputUser")
	public List<UserCredential> findAllUserCredenfial(){
		return userCredentialRepo.findAll();
	}
	
}
