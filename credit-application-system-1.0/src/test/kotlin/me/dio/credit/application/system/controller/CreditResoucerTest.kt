package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.dto.request.CreditDto
import me.dio.credit.application.system.dto.request.CustomerDto
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.repository.CustomerRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import java.time.LocalDate


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClassicCreditControllerTest {

    @Autowired
    private lateinit var creditRepository: CreditRepository

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        const val URL: String = "/api/credits"
    }

    @BeforeEach
    fun setUp() {
        creditRepository.deleteAll()
        customerRepository.deleteAll()
    }

    @AfterEach
    fun tearDown(){
        creditRepository.deleteAll()
        customerRepository.deleteAll()
    }

    @Test
    fun `create credit 201`() {
        // Dado que você já criou um cliente no contexto do teste com ID "1L"
        val customer = customerRepository.save(builderCustomerDto().toEntity())
        val creditDto = buildCreditDto(customerId = customer.id!!)

        val valueAsString: String = objectMapper.writeValueAsString(creditDto)

        // When
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
        //then
        result.andExpect(MockMvcResultMatchers.status().isCreated)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `find all By customerId`() {
        // Dado que você já criou um cliente no contexto do teste com ID "1L"
        val customer = customerRepository.save(builderCustomerDto().toEntity())

        // Crie alguns créditos associados ao cliente
        val creditDto1 = buildCreditDto(customerId = customer.id!!)
        val creditDto2 = buildCreditDto(creditValue = BigDecimal.valueOf(1000.0), customerId = customer.id!!)
        val creditDto3 = buildCreditDto(creditValue = BigDecimal.valueOf(1500.0), customerId = customer.id!!)

        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creditDto1))
        ).andExpect(MockMvcResultMatchers.status().isCreated)

        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creditDto2))
        ).andExpect(MockMvcResultMatchers.status().isCreated)

        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creditDto3))
        ).andExpect(MockMvcResultMatchers.status().isCreated)

        // Quando
        val result = mockMvc.perform(
            MockMvcRequestBuilders.get("$URL?customerId=${customer.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )

        // Então
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `find By Credit Code`() {
        // given
        val customer = customerRepository.save(builderCustomerDto().toEntity())
        val creditDto = buildCreditDto(customerId = customer.id!!)
        val credit = creditRepository.save(creditDto.toEntity())

        // when
        val result = mockMvc.perform(
            MockMvcRequestBuilders.get("$URL/${credit.creditCode}?customerId=${customer.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(MockMvcResultHandlers.print())
    }


    private fun builderCustomerDto(
        firstName: String = "Cami",
        lastName: String = "Santana",
        cpf: String = "28475934625",
        email: String = "camila@email.com",
        income: BigDecimal = BigDecimal.valueOf(1000.0),
        password: String = "1234",
        zipCode: String = "000000",
        street: String = "Rua da Cami, 123",
        id: Long = 1L
    ) = CustomerDto(
        firstName = firstName,
        lastName = lastName,
        cpf = cpf,
        email = email,
        income = income,
        password = password,
        zipCode = zipCode,
        street = street,
        id = id
    )

    private fun buildCreditDto(
        creditValue: BigDecimal = BigDecimal.valueOf(500.0),
        dayFirstOfInstallment: LocalDate = LocalDate.now().plusDays(7),
        numberOfInstallments: Int = 5,
        customerId: Long = 1L
    ): CreditDto = CreditDto(
        creditValue = creditValue,
        dayFirstOfInstallment = dayFirstOfInstallment,
        numberOfInstallments = numberOfInstallments,
        customerId = customerId
    )


}