package oneclass.oneclass.domain.attendance.controller;

import oneclass.oneclass.domain.attendance.dto.response.AttendanceResponse;
import oneclass.oneclass.domain.attendance.entity.AttendanceStatus;
import oneclass.oneclass.domain.attendance.service.AttendanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AdminAttendanceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AttendanceService attendanceService;

    @InjectMocks
    private AdminAttendanceController attendanceController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(attendanceController).build();
    }

    @Test
    void getAttendanceByStatus_returnsList() throws Exception {
        AttendanceResponse response = new AttendanceResponse("Alice", AttendanceStatus.ABSENT, LocalDate.now());
        when(attendanceService.getTodayMembersByStatus(AttendanceStatus.ABSENT))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/attendance")
                        .param("status", "ABSENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].attendanceStatus").value("ABSENT"));
    }
}
