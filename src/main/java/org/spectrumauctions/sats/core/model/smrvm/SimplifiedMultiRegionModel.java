/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.smrvm;

import java.util.List;

import com.google.common.base.Preconditions;
import org.spectrumauctions.sats.core.model.DefaultModel;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

/**
 * @author Michael Weiss
 *
 */
public class SimplifiedMultiRegionModel extends DefaultModel<SMRVMWorld, SMRVMBidder> {
   
    private SMRVMWorldSetup.SMRVMWorldSetupBuilder worldBuilder;
    private SMRVMLocalBidderSetup.Builder localBidderBuilder;
    private SMRVMRegionalBidderSetup.Builder regionalBidderBuilder;
    private SMRVMGlobalBidderSetup.Builder globalBidderBuilder;

    public SimplifiedMultiRegionModel() {
        super();
        this.worldBuilder = new SMRVMWorldSetup.SMRVMWorldSetupBuilder();
        this.localBidderBuilder = new SMRVMLocalBidderSetup.Builder();
        this.regionalBidderBuilder = new SMRVMRegionalBidderSetup.Builder();
        this.globalBidderBuilder = new SMRVMGlobalBidderSetup.Builder();
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.model.QuickDefaultAccess#createWorld(ch.uzh.ifi.ce.mweiss.specval.util.random.RNGSupplier)
     */
    @Override
    public SMRVMWorld createWorld(RNGSupplier worldSeed) {
        return new SMRVMWorld(worldBuilder.build(), worldSeed);
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.model.QuickDefaultAccess#createPopulation(ch.uzh.ifi.ce.mweiss.specval.model.World, ch.uzh.ifi.ce.mweiss.specval.util.random.RNGSupplier)
     */
    @Override
    public List<SMRVMBidder> createPopulation(SMRVMWorld world, RNGSupplier populationRNG) {
        return world.createPopulation(localBidderBuilder.build(), regionalBidderBuilder.build(), globalBidderBuilder.build(), populationRNG);
    }
    
    public void setNumberOfLocalBidders(int number){
        Preconditions.checkArgument(number >= 0);
        localBidderBuilder.setNumberOfBidders(number);
    }
    
    public void setNumberOfRegionalBidders(int number){
        Preconditions.checkArgument(number >= 0);
        regionalBidderBuilder.setNumberOfBidders(number);
    }
    
    public void setNumberOfGlobalBidders(int number){
        Preconditions.checkArgument(number >= 0);
        globalBidderBuilder.setNumberOfBidders(number);
    }

}
