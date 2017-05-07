/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.smrvm;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spectrumauctions.sats.core.model.Bundle;
import static org.spectrumauctions.sats.core.model.mrvm.MRVMRegionsMap.Region;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;


/**
 * @author Michael Weiss
 *
 */
public class SMRVMBidderTypeSpecificTest {
    
    private static SMRVMWorld world;
    
    private static SMRVMLocalBidder localBidder;
    private static SMRVMRegionalBidder regionalBidder;
    private static SMRVMGlobalBidder globalBidder;
    
    private static Bundle<SMRVMLicense> completeBundle;

    @BeforeClass
    public static void beforeClass(){
        world = new SMRVMWorld(SMRVMSimpleWorldGen.getSimpleWorldBuilder(), new JavaUtilRNGSupplier(234872L));
        localBidder = (SMRVMLocalBidder) world.createPopulation(SMRVMSimpleWorldGen.getSimpleLocalBidderSetup(),null,null, new JavaUtilRNGSupplier(234873L)).iterator().next();
        regionalBidder = (SMRVMRegionalBidder) world.createPopulation(null, SMRVMSimpleWorldGen.getSimpleRegionalBidderSetup(), null, new JavaUtilRNGSupplier(3984274L)).iterator().next();
        globalBidder = (SMRVMGlobalBidder) world.createPopulation(null, null, SMRVMSimpleWorldGen.getSimpleGlobalBidderSetup(), new JavaUtilRNGSupplier(29842793L)).iterator().next();
        completeBundle = new Bundle<>(world.getLicenses());
    
    }
    
    @Test
    public void localGammaValuesTestOnCompleteBundle(){
        //Gamma Values if bidder has complete bundle (note that bundle should not have an influence)
        Map<Region, BigDecimal> gammaValues = localBidder.gammaFactors(completeBundle);
        int interestRegionsCount = 0;
        for(Entry<Region, BigDecimal> gammaEntry : gammaValues.entrySet()){
            if(localBidder.regionsOfInterest.contains(gammaEntry.getKey().getId())){
                interestRegionsCount++;
                Assert.assertTrue(BigDecimal.ONE.compareTo(gammaEntry.getValue()) == 0);
            }
        }
        Assert.assertEquals(3, interestRegionsCount);
    }
    
    /**
     * Note: Validity of distance calculation is tested in {@link SMRVMWorldTest#regionalDistanceTests()}
     */
    @Test
    public void regionalGammaValuesTest(){
        //First constraint: All regional gammas have to be in (0,1] and not dependent on bundle
        Map<Region, BigDecimal> calculatedGammas = regionalBidder.gammaFactors(null);
        for(Entry<Region, BigDecimal> regionEntry : calculatedGammas.entrySet()){
            if(regionEntry.getKey().equals(regionalBidder.home)){
                Assert.assertTrue(regionEntry.getValue().compareTo(BigDecimal.ONE) == 0);
            }else{
                Assert.assertTrue(regionEntry.getValue().compareTo(BigDecimal.ONE) < 0);
                Assert.assertTrue(regionEntry.getValue().compareTo(BigDecimal.ZERO) > 0);
            }
        }
        //TODO actual calculation on any region
    }
    
    @Test
    public void globalGammaValuesTestOnCompleteBundle(){
        //Gamma Value for the complete bundle should be 1, i.e., there is no discount if global bidder has all licenses
        BigDecimal returnedGammaValue = globalBidder.gammaFactor(null, completeBundle);
        Assert.assertTrue(BigDecimal.ONE.compareTo(returnedGammaValue) == 0);
        for(BigDecimal gammaFromMap : globalBidder.gammaFactors(completeBundle).values()){
            Assert.assertTrue(BigDecimal.ONE.compareTo(gammaFromMap) == 0);
        }      
    }
    
    @Test
    public void emptyBundleTest(){
        Bundle<SMRVMLicense> emptyBundle = new Bundle<>();
        Assert.assertEquals(BigDecimal.ZERO,localBidder.calculateValue(emptyBundle));
        Assert.assertEquals(BigDecimal.ZERO,globalBidder.calculateValue(emptyBundle));
        Assert.assertEquals(BigDecimal.ZERO,regionalBidder.calculateValue(emptyBundle));
    }
    
    
    
    
    
    
}
