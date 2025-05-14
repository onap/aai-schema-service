package org.onap.aai.schemagen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SpringContextAwareTest {

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private SpringContextAware springContextAware;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSetApplicationContext() {
        // Given
        // We are mocking the application context, so no need to do anything here.

        // When
        springContextAware.setApplicationContext(applicationContext);

        // Then
        assertNotNull(SpringContextAware.getApplicationContext(), "Application context should be set.");
        assertEquals(applicationContext, SpringContextAware.getApplicationContext(), "Application context is not correct.");
    }

    @Test
    public void testGetBeanByNameAndType() {
        // Given
        String beanName = "myBean";
        MyBean mockBean = mock(MyBean.class);
        when(applicationContext.getBean(beanName, MyBean.class)).thenReturn(mockBean);
        springContextAware.setApplicationContext(applicationContext);

        // When
        MyBean result = SpringContextAware.getBean(beanName, MyBean.class);

        // Then
        assertNotNull(result, "Bean should not be null.");
        assertEquals(mockBean, result, "Bean returned is not the expected one.");
        verify(applicationContext, times(1)).getBean(beanName, MyBean.class);
    }

    @Test
    public void testGetBeanByType() {
        // Given
        MyBean mockBean = mock(MyBean.class);
        when(applicationContext.getBean(MyBean.class)).thenReturn(mockBean);
        springContextAware.setApplicationContext(applicationContext);

        // When
        MyBean result = SpringContextAware.getBean(MyBean.class);

        // Then
        assertNotNull(result, "Bean should not be null.");
        assertEquals(mockBean, result, "Bean returned is not the expected one.");
        verify(applicationContext, times(1)).getBean(MyBean.class);
    }

    @Test
    public void testGetBeanByName() {
        // Given
        String beanName = "myBean";
        MyBean mockBean = mock(MyBean.class);
        when(applicationContext.getBean(beanName)).thenReturn(mockBean);
        springContextAware.setApplicationContext(applicationContext);

        // When
        Object result = SpringContextAware.getBean(beanName);

        // Then
        assertNotNull(result, "Bean should not be null.");
        assertEquals(mockBean, result, "Bean returned is not the expected one.");
        verify(applicationContext, times(1)).getBean(beanName);
    }

    @Test
    public void testGetBeanWhenContextIsNull() {
        // Given
        // Simulate a null application context (SpringContextAware is not initialized).
        SpringContextAware springContextAwareWithoutContext = new SpringContextAware();

        // When and Then
        assertNull(SpringContextAware.getBean(MyBean.class), "Bean should be null when context is not set.");
        assertNull(SpringContextAware.getBean("myBean"), "Bean should be null when context is not set.");
        assertNull(SpringContextAware.getBean("myBean", MyBean.class), "Bean should be null when context is not set.");
    }

    // Example class for testing purposes
    public static class MyBean {
        // Some properties and methods for testing purposes
    }
}
