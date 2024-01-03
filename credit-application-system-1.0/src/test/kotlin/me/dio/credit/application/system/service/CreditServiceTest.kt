package me.dio.credit.application.system.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.exception.BusinessException
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.repository.CustomerRepository
import me.dio.credit.application.system.service.impl.CreditService
import me.dio.credit.application.system.service.impl.CustomerService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.util.*

@ExtendWith(MockKExtension::class)
class CreditServiceTest {
    @MockK
    lateinit var creditRepository: CreditRepository

    @InjectMockKs
    lateinit var creditService: CreditService

    @MockK
    lateinit var customerRepository: CustomerRepository

    @MockK
    lateinit var customerService: CustomerService

    //Aqui ficara as funçoes
    @Test
    fun `save a crdeit`() {
        //given
        val fakeCredit: Credit = buildCredit()
        every { creditRepository.save(any()) } returns fakeCredit
        every { customerService.findById(fakeCredit.customer?.id!!) } returns fakeCredit.customer!!
        //when
        val actual: Credit = creditService.save(fakeCredit)
        //then
        Assertions.assertThat(actual).isNotNull
        Assertions.assertThat(actual).isSameAs(fakeCredit)
        verify(exactly = 1) {
            creditRepository.save(fakeCredit)
        }
        verify(exactly = 1) {
            customerService.findById(fakeCredit.customer?.id!!)
        }
    }


    @Test
    fun `should find all credits for a given customer`() {
        // given
        val fakeCustomerId: Long = 1L
        val fakeCredits: List<Credit> = listOf(buildCredit(), buildCredit())
        every { creditRepository.findAllByCustomerId(fakeCustomerId) } returns fakeCredits

        // when
        val actual: List<Credit> = creditService.findAllByCustomer(fakeCustomerId)

        // then
        Assertions.assertThat(actual).isNotNull
        Assertions.assertThat(actual).isEqualTo(fakeCredits)
        verify(exactly = 1) { creditRepository.findAllByCustomerId(fakeCustomerId) }
    }

    //agora criar uma funçao para lançar uma exceçao caos tenha um erro
    @Test
    fun `should throw BusinessException when credit is not found by code`() {
        // given
        val fakeCustomerId: Long = 1L
        val fakeCreditCode: UUID = UUID.randomUUID()
        every { creditRepository.findByCreditCode(fakeCreditCode) } returns null

        // when
        // then
        Assertions.assertThatExceptionOfType(BusinessException::class.java)
            .isThrownBy { creditService.findByCreditCode(fakeCustomerId, fakeCreditCode) }
            .withMessage("Creditcode $fakeCreditCode not found")

        verify(exactly = 1) { creditRepository.findByCreditCode(fakeCreditCode) }
    }


    //para criar o customer dono do credit fake
    private fun buildCustomer(
        firstName: String = "Cami",
        lastName: String = "Cavalcante",
        cpf: String = "28475934625",
        email: String = "camila@gmail.com",
        password: String = "12345",
        zipCode: String = "12345",
        street: String = "Rua da Cami",
        income: BigDecimal = BigDecimal.valueOf(1000.0),
        id: Long = 1L
    ) = Customer(
        firstName = firstName,
        lastName = lastName,
        cpf = cpf,
        email = email,
        password = password,
        address = Address(
            zipCode = zipCode,
            street = street,
        ),
        income = income,
        id = id
    )
        //Para criar o credit fake para fazer os teste
    private fun buildCredit(
        creditValue: BigDecimal = BigDecimal.valueOf(500.0),
        dayFirstInstallment: LocalDate = LocalDate.of(2023, Month.APRIL, 22),
        numberOfInstallments: Int = 5,
        customer: Customer = buildCustomer()
    ): Credit = Credit(
        creditValue = creditValue,
        dayFirstInstallment = dayFirstInstallment,
        numberOfInstallments = numberOfInstallments,
        customer = customer
    )
}