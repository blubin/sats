/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.bm.mbvm;

import java.util.ArrayList;
import java.util.List;

import org.spectrumauctions.sats.core.model.DefaultModel;
import org.spectrumauctions.sats.core.model.bm.BMBidder;
import org.spectrumauctions.sats.core.model.bm.BMBidderSetup;
import org.spectrumauctions.sats.core.model.bm.BMWorld;
import org.spectrumauctions.sats.core.model.bm.bvm.BaseValueModel;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import com.google.common.base.Preconditions;

/**
 * @author Michael Weiss
 *
 */
public class MultiBandValueModel extends DefaultModel<BMWorld, BMBidder> {
    
    MBVMWorldSetup.MBVMWorldSetupBuilder worldSetupBuilder;
    MBVMBidderSetup.MBVMBidderSetupBuilder bidderSetupBuilder;
    
    
    /**
     * Creates a new QuickAccessor to the MultiBand Value Model in its default configuration.
     * Note: Since the orginal model definition does not specify the number of bidders, 
     * it is recommended to use {@link BaseValueModel#BaseValueModel(int)} for construction.
     * Otherwise, 5 bidders are created by default.
     */
    public MultiBandValueModel(){
        this(5);
    }
    
    /**
     * Creates a new QuickAccessor to the MultiBand Value Model in its default configuration.
     * @param numberOfBidders specifies how many bidders there will be created in each population
     */
    public MultiBandValueModel(int numberOfBidders){
        worldSetupBuilder = new MBVMWorldSetup.MBVMWorldSetupBuilder(MBVMWorldSetup.DEFAULT_SETUP_NAME);
        bidderSetupBuilder = new MBVMBidderSetup.MBVMBidderSetupBuilder(MBVMBidderSetup.DEFAULT_SETUP_NAME, numberOfBidders);
    }
    
    /* (non-Javadoc)
     * @see org.spectrumauctions.sats.core.model.QuickDefaultAccess#createWorld(RNGSupplier)
     */
    @Override
    public BMWorld createWorld(RNGSupplier rngSupplier) {
        return new BMWorld(worldSetupBuilder.build(), rngSupplier);
    }

    /* (non-Javadoc)
     * @see org.spectrumauctions.sats.core.model.QuickDefaultAccess#createPopulation(World, RNGSupplier)
     */
    @Override
    public List<BMBidder> createPopulation(BMWorld world, RNGSupplier populationRNG) {
        List<BMBidderSetup> setupset = new ArrayList<>();
        setupset.add(bidderSetupBuilder.build());
        return world.createPopulation(setupset, populationRNG);
    }
    

    /**
     * @return The number of bidders to be created
     */
    public int getNumberOfBidders() {
        return bidderSetupBuilder.getNumberOfBidders();
    }

    /**
     * Set the number of bidders to be created
     * @param numberOfBidders
     */
    public void setNumberOfBidders(int numberOfBidders) {
        Preconditions.checkArgument(numberOfBidders > 0);
        bidderSetupBuilder.setNumberOfBidders(numberOfBidders);
    }
    
    protected void modifyWorldBuilder(){
        worldSetupBuilder.setSetupName("MODIFIED_MBVM");
    }
    
    protected void modifyBidderBuilder(){
        bidderSetupBuilder.setSetupName("MODIFIED_MBVM");
    }
}