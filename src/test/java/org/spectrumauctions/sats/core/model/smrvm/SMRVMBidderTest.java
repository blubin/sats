/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.smrvm;

import java.math.BigDecimal;
import java.math.MathContext;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.spectrumauctions.sats.core.model.mrvm.MRVMRegionsMap.Region;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;

/**
 * @author Michael Weiss
 *
 */
public class SMRVMBidderTest {

    private static SMRVMWorld world;
    private static SMRVMBidder bidder;
    
    
    /**
     * @throws Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        world = new SMRVMWorld(SMRVMSimpleWorldGen.getSimpleWorldBuilder(), new JavaUtilRNGSupplier(9023480L));
        bidder = world.createPopulation(SMRVMSimpleWorldGen.getSimpleLocalBidderSetup(), null, null, new JavaUtilRNGSupplier(234234)).iterator().next();
    }

   
    /**
     * Test method for {@link SMRVMBidder#quality(BigDecimal)}.
     */
    @Test
    @Ignore
    public void testQuality() {
        //Beta = 0.5
        
        //Quality if no capacity
        BigDecimal quality = bidder.quality(BigDecimal.ZERO);
        Assert.assertTrue(quality.compareTo(BigDecimal.valueOf(0.25)) == 0);
        //Quality if half capacity
        quality = bidder.quality(BigDecimal.valueOf(0.5));
        Assert.assertTrue(quality.compareTo(BigDecimal.valueOf(0.5)) == 0);
        //Quality if full capacity
        quality= bidder.quality(BigDecimal.ONE);
        Assert.assertTrue(quality.compareTo(BigDecimal.valueOf(0.75)) == 0);
    }

    /**
     * Test method for {@link SMRVMBidder#omegaFactor(Region, BigDecimal)}.
     */
    @Test
    public void testOmegaFactor() {


        BigDecimal quality = BigDecimal.ONE;
        testOmegaWithQuality(quality);
        
        quality = new BigDecimal(0.75);
        testOmegaWithQuality(quality);
        
        quality = new BigDecimal(0.5);
        testOmegaWithQuality(quality);
        
        quality = new BigDecimal(0.25);
        testOmegaWithQuality(quality);
        
        quality = BigDecimal.ZERO;
        testOmegaWithQuality(quality);
        
        
    }
    
    
    private void testOmegaWithQuality(BigDecimal quality){
        // Population is 100
        BigDecimal pop = new BigDecimal(100);
        // beta is 0.5
        BigDecimal beta = BigDecimal.valueOf(0.5);
        // alpha is 0.4
        BigDecimal alpha = BigDecimal.valueOf(0.4);
        // all regions have the same population
        Region anyRegion = bidder.getWorld().getRegionsMap().getRegion(0);
        
        // Test with quality = 1;
        BigDecimal expectedOmega = pop.multiply(beta).multiply(alpha).multiply(quality);
        expectedOmega = expectedOmega.round(new MathContext(20));
        BigDecimal calculatedOmega = bidder.omegaFactor(anyRegion, quality);
        calculatedOmega = calculatedOmega.round(new MathContext(7));
        Assert.assertTrue("Expected was " + expectedOmega + " result was " + calculatedOmega, expectedOmega.compareTo(calculatedOmega) == 0);
    }



}
