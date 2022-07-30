package exchange.lob.fix.incoming;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.InOrder;

import java.io.UnsupportedEncodingException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


public final class FixTagParserTest
{

    private FixTagHandler fixTagHandler;

    @BeforeEach
    public void setUp()
    {
        fixTagHandler = mock(FixTagHandler.class);
    }

    @Test
    public void shouldCountTagsCorrectlyInMessage()
    {

        given(fixTagHandler.isFinished()).willReturn(false);

        final byte[] logonMsg = FixMessageUtil.getLogonMessage();

        final FixTagParser parser = new FixTagParser(fixTagHandler);

        parser.parse(logonMsg, 0, logonMsg.length, true);

        verify(fixTagHandler).messageStart();
        verify(fixTagHandler, times(13)).onTag(any(int.class), any(byte[].class), any(int.class), any(int.class));
        verify(fixTagHandler, times(13)).isFinished();
        verify(fixTagHandler).messageEnd();

        verifyNoMoreInteractions(fixTagHandler);
    }

    @Test
    public void shouldCountTagsSeparatedByMultipleSeparatorsMessage() throws UnsupportedEncodingException
    {
        given(fixTagHandler.isFinished()).willReturn(false);

        final byte[] msg = "8=FIX.4.2\u00019=105|35=A|34=1|49=marketm155yjtfwicmfe\u000152=20100713-17:04:39.641|56=FIX-API|95=9|96=P4ssword.|98=0\u0001108=2|141=Y|10=191|".getBytes("US-ASCII");
        final byte[] logonMsg = new byte[msg.length];
        System.arraycopy(msg, 0, logonMsg, 0, msg.length);

        final FixTagParser parser = new FixTagParser(fixTagHandler, new byte[]{1, 124});

        parser.parse(logonMsg, 0, logonMsg.length, true);

        verify(fixTagHandler).messageStart();
        verify(fixTagHandler, times(13)).onTag(any(int.class), any(byte[].class), any(int.class), any(int.class));
        verify(fixTagHandler, times(13)).isFinished();
        verify(fixTagHandler).messageEnd();

        verifyNoMoreInteractions(fixTagHandler);
    }

    @Test
    public void shouldReportFirstThreeTags()
    {
        given(fixTagHandler.isFinished()).willReturn(false);

        final byte[] logonMsg = FixMessageUtil.getLogonMessage();

        final FixTagParser parser = new FixTagParser(fixTagHandler);

        parser.parse(logonMsg, 0, 21, true);

        verify(fixTagHandler).messageStart();
        verify(fixTagHandler).onTag(8, logonMsg, 2, 7);
        verify(fixTagHandler).onTag(9, logonMsg, 12, 3);
        verify(fixTagHandler).onTag(35, logonMsg, 19, 1);
        verify(fixTagHandler, times(3)).isFinished();
        verify(fixTagHandler).messageEnd();

        verifyNoMoreInteractions(fixTagHandler);
    }

    @Test
    public void shouldStopReadingWhenTheMessageIsNotLogon()
    {
        final byte[] newOrderSingleMsg = FixMessageUtil.getNewOrderSingle();

        final InOrder inOrder = BDDMockito.inOrder(fixTagHandler);

        given(fixTagHandler.isFinished()).willReturn(false).willReturn(false).willReturn(true);

        final FixTagParser parser = new FixTagParser(fixTagHandler);
        parser.parse(newOrderSingleMsg, 0, newOrderSingleMsg.length, true);

        inOrder.verify(fixTagHandler).messageStart();

        inOrder.verify(fixTagHandler).onTag(8, newOrderSingleMsg, 2, 7);
        inOrder.verify(fixTagHandler, times(1)).isFinished();

        inOrder.verify(fixTagHandler).onTag(9, newOrderSingleMsg, 12, 3);
        inOrder.verify(fixTagHandler, times(1)).isFinished();

        inOrder.verify(fixTagHandler).onTag(35, newOrderSingleMsg, 19, 1);
        inOrder.verify(fixTagHandler, times(1)).isFinished();

        inOrder.verify(fixTagHandler).messageEnd();
    }
}
