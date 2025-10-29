package oneclass.oneclass.global.exception;

import oneclass.oneclass.domain.attendance.controller.AdminAttendanceController;
import oneclass.oneclass.domain.attendance.entity.AttendanceStatus;
import oneclass.oneclass.domain.attendance.service.AttendanceService;
import oneclass.oneclass.domain.member.error.MemberError;
import oneclass.oneclass.global.auth.jwt.JwtFilter;
import oneclass.oneclass.global.auth.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminAttendanceController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AttendanceService attendanceService;

    @TestConfiguration
    static class MockServiceConfig {
        @Bean
        public AttendanceService attendanceService() {
            return Mockito.mock(AttendanceService.class);
        }

        @Bean
        public JwtProvider jwtProvider() {
            return Mockito.mock(JwtProvider.class);
        }

        @Bean
        public JwtFilter jwtFilter(JwtProvider jwtProvider) {
            return new JwtFilter(jwtProvider);
        }
    }

    @BeforeEach
    void setUp() {
        Mockito.reset(attendanceService);

        Mockito.when(attendanceService.getTodayMembersByStatus(AttendanceStatus.PRESENT))
                .thenReturn(Collections.emptyList());

        Mockito.when(attendanceService.getAttendanceByDate(any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        Mockito.when(attendanceService.getAttendanceByMember(Mockito.anyLong()))
                .thenReturn(Collections.emptyList());
    }

    @Test
    void testValidRequest() throws Exception {
        var result = mockMvc.perform(get("/attendance")
                        .param("status", "PRESENT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println("---- testValidRequest ----");
        System.out.println("Status: " + result.getResponse().getStatus());
        System.out.println("Response: " + result.getResponse().getContentAsString());
    }

    @Test
    void testInvalidEnumStatus() throws Exception {
        var result = mockMvc.perform(get("/attendance")
                        .param("status", "INVALID_STATUS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        System.out.println("---- testInvalidEnumStatus ----");
        System.out.println("Status: " + result.getResponse().getStatus());
        System.out.println("Response: " + result.getResponse().getContentAsString());
    }

    @Test
    void testInvalidDateFormat() throws Exception {
        var result = mockMvc.perform(get("/attendance/date/2025-99-99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        System.out.println("---- testInvalidDateFormat ----");
        System.out.println("Status: " + result.getResponse().getStatus());
        System.out.println("Response: " + result.getResponse().getContentAsString());
    }

    @Test
    void testMemberNotFound() throws Exception {
        Mockito.when(attendanceService.getAttendanceByMember(Mockito.anyLong()))
                .thenThrow(new CustomException(MemberError.NOT_FOUND));

        var result = mockMvc.perform(get("/attendance/member/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        System.out.println("---- testMemberNotFound ----");
        System.out.println("Status: " + result.getResponse().getStatus());
        System.out.println("Response: " + result.getResponse().getContentAsString());
    }

    @Test
    void testGenericException() throws Exception {
        Mockito.when(attendanceService.getAttendanceByDate(any(LocalDate.class)))
                .thenThrow(new RuntimeException("서버 내부 오류"));

        var result = mockMvc.perform(get("/attendance/date/2025-10-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andReturn();

        System.out.println("---- testGenericException ----");
        System.out.println("Status: " + result.getResponse().getStatus());
        System.out.println("Response: " + result.getResponse().getContentAsString());
    }
}
