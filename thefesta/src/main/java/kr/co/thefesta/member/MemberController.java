package kr.co.thefesta.member;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController; // RestController
import org.springframework.web.multipart.MultipartFile;

import kr.co.thefesta.member.domain.MemberDTO;
import kr.co.thefesta.member.service.IMemberService;
import kr.co.thefesta.member.until.MailUtil;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping("/member")
@CrossOrigin(origins = "*")
@Log4j
public class MemberController {

	@Autowired
	private IMemberService service;
	
	@PostMapping(value = "/loginPost") // o
	public MemberDTO loginPost(@RequestBody MemberDTO mDto, HttpSession session) throws Exception {
		MemberDTO memInfo = service.login(mDto);
		log.info("MemberDTO ==> " + memInfo);
		
		if (memInfo == null) {
//			return "redirect:/member/login"; 리액트에서 처리
		} else {
			session.setAttribute("loginInfo", memInfo);
			log.info("session......" + session.getAttribute("loginInfo").toString());
//			return "redirect:/member/login"; 리액트에서 처리
		}
		return memInfo;
	}

	
	@GetMapping("/logout")
	public void logout(HttpSession session) throws Exception {
		log.info("logout......");
		Object obj = session.getAttribute("loginInfo");
		
		if (obj != null) {
			
			MemberDTO memInfo = (MemberDTO) obj;
	        String id = memInfo.getId();
	        
			service.updateLogDate(id);
			
			session.removeAttribute("loginInfo");
			session.invalidate();
		}
	}
	
	
	@RequestMapping(value = "/nicknameCheck", method = RequestMethod.POST)
    public String nicknameCheck(@RequestBody MemberDTO mDto) throws Exception {
        String nickname = mDto.getNickname();
		int nicknameCheck = service.nicknameCheck(nickname);
        String nickCheckResult = "fail";
        
        if(nicknameCheck != 0) {
        	log.info("fail");
        	return nickCheckResult;

		} else {
			log.info("success");
			nickCheckResult = "success";
			return nickCheckResult;
		}
    }
	
	@RequestMapping(value = "/idCheck", method = RequestMethod.POST)
	public String idCheck(@RequestBody MemberDTO mDto) throws Exception {
		String id = mDto.getId();
		int idCheck = service.idCheck(id);
		String idResult = "fail";
		
		if(idCheck != 0) {
			log.info("fail");
			return idResult;
			
		} else {
			log.info("success");
			idResult = "success";
			return idResult;
		}
	}
	
	// 멤버 상태코드를 조회해 아이디 중복검사 진행
	@RequestMapping(value = "/selMember", method = RequestMethod.POST)
	public String selMember(@RequestBody String id) throws Exception {
		MemberDTO selMember = service.selMember(id);
		String stateCode = selMember.getStatecode();
//		int idCheck = service.idCheck(id);
		
		return stateCode;
	}
	
	@RequestMapping(value = "/mailSend", method = RequestMethod.POST)
	public String sendMail(@RequestBody MemberDTO mDto) throws Exception {
		String randomCode = randomCode();
		String id = mDto.getId();
		
		String title = "TheFesta 인증번호 전송";
		String from = "dain7362@naver.com";
		String text = "인증번호는 " + randomCode + " 입니다.";
		String to = id;
		String cc = "";
		
		MailUtil.mailSend(title, from, text, to, cc);
		return randomCode;
	}
	
	private String randomCode() {
        Random random = new Random();
        int randomCode = 100000 + random.nextInt(900000);
        return String.valueOf(randomCode);
    }
	
	@PostMapping(value = "/joinPost")
	public void joinPost(@RequestBody MemberDTO mDto) throws Exception {
		service.join(mDto);			
	}
	
	
	@PostMapping(value = "/pwReset")
	public void pwReset(@RequestBody MemberDTO mDto) throws Exception {
		Map<String, Object> paramMap = new HashMap<>();
		
		String id = mDto.getId();
		String password = mDto.getPassword();
		
		paramMap.put("id", id);
		paramMap.put("password", password);
		
		service.pwReset(paramMap);
	}
	
	
	
	@PostMapping(value = "/memInfoReset")
	public String memInfoReset(@RequestBody MemberDTO mDto, HttpSession session) throws Exception {
		
		log.info(mDto);
		
		if (mDto.getNickname() == null) {
			String nickname = (String) session.getAttribute("nickname");
			mDto.setNickname(nickname);
	    }
		if (mDto.getResetPassword() != null) {
			String resetPassword = (String) session.getAttribute("resetPassword");
			mDto.setResetPassword(resetPassword);
		}
		service.memInfoReset(mDto);
		return ""; // 수정 필요
	}
	

	@PostMapping("/updateImg")
	public String updateImg(@RequestParam String id, @RequestParam MultipartFile file) throws Exception {
	    log.info("id" + id);

	    Map<String, Object> paramMap = new HashMap<>();

	    if (!file.isEmpty()) {
	        String saveImg = "D:\\workspace\\spring4-4.10.0.RELEASE\\thefestaTest\\src\\main\\webapp\\resources\\fileUpload\\" + file.getOriginalFilename();
	        log.info("파일 저장" + saveImg);
	        file.transferTo(new File(saveImg));

	        // 파일이 실제로 저장되었는지 확인
	        File savedFile = new File(saveImg);
	        if (savedFile.exists()) {
	            log.info("파일이 성공적으로 저장되었습니다.");

	            String profileImg = "http://localhost:9090/resources/fileUpload/" + file.getOriginalFilename();
	            log.info("테스트" + profileImg + id);
	            paramMap.put("profileImg", profileImg);
	            paramMap.put("id", id);
	            service.updateImg(paramMap);
	            log.info(paramMap);

	            return profileImg;
	        } else {
	            log.error("파일 저장에 실패했습니다.");
	        }
	    }
	    return "파일이 비어있습니다.";
	}
	
	
	@PostMapping(value = "/updateState")
	public void updateState(@RequestBody MemberDTO mDto) throws Exception {
		String id = mDto.getId();
		String statecode = mDto.getStatecode();
		
		service.updateState(mDto);
		log.info(mDto);
	}
}