import React from 'react';
import { FormGroup, Label, Input, FormText, Row, Col, Card, CardHeader, CardBody } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { ApplicationProperties } from 'app/shared/model/application-configuration.model';

interface ChainConfigPanelProps {
  config: ApplicationProperties;
  onChange: (config: ApplicationProperties) => void;
  errors?: any;
}

const ChainConfigPanel: React.FC<ChainConfigPanelProps> = ({ config, onChange, errors = {} }) => {
  const handleChainChange = (field: string, value: any) => {
    onChange({
      ...config,
      chain: {
        ...config.chain,
        [field]: value,
      },
    });
  };

  const handleWalletChange = (field: string, value: any) => {
    onChange({
      ...config,
      chain: {
        ...config.chain,
        wallet: {
          ...config.chain?.wallet,
          [field]: value,
        },
      },
    });
  };

  const handleSnapshotSyncChange = (field: string, value: any) => {
    onChange({
      ...config,
      chain: {
        ...config.chain,
        snapshotSync: {
          ...config.chain?.snapshotSync,
          [field]: value,
        },
      },
    });
  };

  return (
    <div className="chain-config-panel">
      <Card className="mb-4">
        <CardHeader>
          <h5 className="mb-0">
            <Translate contentKey="applicationConfiguration.chain.title">Blockchain Configuration</Translate>
          </h5>
        </CardHeader>
        <CardBody>
          <FormGroup check className="mb-3">
            <Label check>
              <Input
                type="checkbox"
                checked={config.chain?.enabled || false}
                onChange={e => handleChainChange('enabled', e.target.checked)}
              />{' '}
              <Translate contentKey="applicationConfiguration.chain.fields.enabled.label">Enable Blockchain</Translate>
            </Label>
            <FormText color="muted">
              <Translate contentKey="applicationConfiguration.chain.fields.enabled.help">Enable blockchain integration</Translate>
            </FormText>
          </FormGroup>

          <Row>
            <Col md={6}>
              <FormGroup>
                <Label for="rpcUrl">
                  <Translate contentKey="applicationConfiguration.chain.fields.rpcUrl.label">RPC URL</Translate>
                </Label>
                <Input
                  type="text"
                  name="rpcUrl"
                  id="rpcUrl"
                  value={config.chain?.rpcUrl || ''}
                  onChange={e => handleChainChange('rpcUrl', e.target.value)}
                  invalid={!!errors.chain?.rpcUrl}
                />
                <FormText color="muted">
                  <Translate contentKey="applicationConfiguration.chain.fields.rpcUrl.help">Blockchain RPC endpoint URL</Translate>
                </FormText>
                {errors.chain?.rpcUrl && <FormText color="danger">{errors.chain.rpcUrl}</FormText>}
              </FormGroup>
            </Col>
            <Col md={6}>
              <FormGroup>
                <Label for="trailHeadBlocks">
                  <Translate contentKey="applicationConfiguration.chain.fields.trailHeadBlocks.label">Trail Head Blocks</Translate>
                </Label>
                <Input
                  type="number"
                  name="trailHeadBlocks"
                  id="trailHeadBlocks"
                  value={config.chain?.trailHeadBlocks || 10}
                  onChange={e => handleChainChange('trailHeadBlocks', parseInt(e.target.value, 10))}
                  min="0"
                  invalid={!!errors.chain?.trailHeadBlocks}
                />
                <FormText color="muted">
                  <Translate contentKey="applicationConfiguration.chain.fields.trailHeadBlocks.help">
                    Number of blocks to trail behind the head
                  </Translate>
                </FormText>
                {errors.chain?.trailHeadBlocks && <FormText color="danger">{errors.chain.trailHeadBlocks}</FormText>}
              </FormGroup>
            </Col>
          </Row>

          <FormGroup>
            <Label for="routerAddress">
              <Translate contentKey="applicationConfiguration.chain.fields.routerAddress.label">Router Address</Translate>
            </Label>
            <Input
              type="text"
              name="routerAddress"
              id="routerAddress"
              value={config.chain?.routerAddress || ''}
              onChange={e => handleChainChange('routerAddress', e.target.value)}
              invalid={!!errors.chain?.routerAddress}
            />
            <FormText color="muted">
              <Translate contentKey="applicationConfiguration.chain.fields.routerAddress.help">Smart contract router address</Translate>
            </FormText>
            {errors.chain?.routerAddress && <FormText color="danger">{errors.chain.routerAddress}</FormText>}
          </FormGroup>
        </CardBody>
      </Card>

      {/* Wallet Configuration */}
      <Card className="mb-4">
        <CardHeader>
          <h6 className="mb-0">
            <Translate contentKey="applicationConfiguration.chain.wallet.title">Wallet Configuration</Translate>
          </h6>
        </CardHeader>
        <CardBody>
          <Row>
            <Col md={6}>
              <FormGroup>
                <Label for="maxGasLimit">
                  <Translate contentKey="applicationConfiguration.chain.wallet.fields.maxGasLimit.label">Max Gas Limit</Translate>
                </Label>
                <Input
                  type="number"
                  name="maxGasLimit"
                  id="maxGasLimit"
                  value={config.chain?.wallet?.maxGasLimit || 1000000}
                  onChange={e => handleWalletChange('maxGasLimit', parseInt(e.target.value, 10))}
                  min="1"
                  invalid={!!errors.chain?.wallet?.maxGasLimit}
                />
                <FormText color="muted">
                  <Translate contentKey="applicationConfiguration.chain.wallet.fields.maxGasLimit.help">
                    Maximum gas limit for transactions
                  </Translate>
                </FormText>
                {errors.chain?.wallet?.maxGasLimit && <FormText color="danger">{errors.chain.wallet.maxGasLimit}</FormText>}
              </FormGroup>
            </Col>
            <Col md={6}>
              <FormGroup>
                <Label for="paymentAddress">
                  <Translate contentKey="applicationConfiguration.chain.wallet.fields.paymentAddress.label">Payment Address</Translate>
                </Label>
                <Input
                  type="text"
                  name="paymentAddress"
                  id="paymentAddress"
                  value={config.chain?.wallet?.paymentAddress || ''}
                  onChange={e => handleWalletChange('paymentAddress', e.target.value)}
                  invalid={!!errors.chain?.wallet?.paymentAddress}
                />
                <FormText color="muted">
                  <Translate contentKey="applicationConfiguration.chain.wallet.fields.paymentAddress.help">
                    Address for receiving payments
                  </Translate>
                </FormText>
                {errors.chain?.wallet?.paymentAddress && <FormText color="danger">{errors.chain.wallet.paymentAddress}</FormText>}
              </FormGroup>
            </Col>
          </Row>

          <FormGroup>
            <Label for="privateKey">
              <Translate contentKey="applicationConfiguration.chain.wallet.fields.privateKey.label">Private Key</Translate>
            </Label>
            <Input
              type="password"
              name="privateKey"
              id="privateKey"
              value={config.chain?.wallet?.privateKey || ''}
              onChange={e => handleWalletChange('privateKey', e.target.value)}
              invalid={!!errors.chain?.wallet?.privateKey}
            />
            <FormText color="muted">
              <Translate contentKey="applicationConfiguration.chain.wallet.fields.privateKey.help">
                Wallet private key (keep secure)
              </Translate>
            </FormText>
            {errors.chain?.wallet?.privateKey && <FormText color="danger">{errors.chain.wallet.privateKey}</FormText>}
          </FormGroup>
        </CardBody>
      </Card>

      {/* Snapshot Sync Configuration */}
      <Card className="mb-4">
        <CardHeader>
          <h6 className="mb-0">
            <Translate contentKey="applicationConfiguration.chain.snapshotSync.title">Snapshot Synchronization</Translate>
          </h6>
        </CardHeader>
        <CardBody>
          <Row>
            <Col md={6}>
              <FormGroup>
                <Label for="syncSleep">
                  <Translate contentKey="applicationConfiguration.chain.snapshotSync.fields.sleep.label">Sleep Duration</Translate>
                </Label>
                <Input
                  type="number"
                  name="syncSleep"
                  id="syncSleep"
                  value={config.chain?.snapshotSync?.sleep || 1.0}
                  onChange={e => handleSnapshotSyncChange('sleep', parseFloat(e.target.value))}
                  step="0.1"
                  min="0"
                  invalid={!!errors.chain?.snapshotSync?.sleep}
                />
                <FormText color="muted">
                  <Translate contentKey="applicationConfiguration.chain.snapshotSync.fields.sleep.help">
                    Sleep time between sync operations (seconds)
                  </Translate>
                </FormText>
                {errors.chain?.snapshotSync?.sleep && <FormText color="danger">{errors.chain.snapshotSync.sleep}</FormText>}
              </FormGroup>
            </Col>
            <Col md={6}>
              <FormGroup>
                <Label for="batchSize">
                  <Translate contentKey="applicationConfiguration.chain.snapshotSync.fields.batchSize.label">Batch Size</Translate>
                </Label>
                <Input
                  type="number"
                  name="batchSize"
                  id="batchSize"
                  value={config.chain?.snapshotSync?.batchSize || 100}
                  onChange={e => handleSnapshotSyncChange('batchSize', parseInt(e.target.value, 10))}
                  min="1"
                  invalid={!!errors.chain?.snapshotSync?.batchSize}
                />
                <FormText color="muted">
                  <Translate contentKey="applicationConfiguration.chain.snapshotSync.fields.batchSize.help">
                    Number of items to process in each batch
                  </Translate>
                </FormText>
                {errors.chain?.snapshotSync?.batchSize && <FormText color="danger">{errors.chain.snapshotSync.batchSize}</FormText>}
              </FormGroup>
            </Col>
          </Row>

          <Row>
            <Col md={6}>
              <FormGroup>
                <Label for="startingSubId">
                  <Translate contentKey="applicationConfiguration.chain.snapshotSync.fields.startingSubId.label">
                    Starting Subscription ID
                  </Translate>
                </Label>
                <Input
                  type="number"
                  name="startingSubId"
                  id="startingSubId"
                  value={config.chain?.snapshotSync?.startingSubId || 0}
                  onChange={e => handleSnapshotSyncChange('startingSubId', parseInt(e.target.value, 10))}
                  min="0"
                  invalid={!!errors.chain?.snapshotSync?.startingSubId}
                />
                <FormText color="muted">
                  <Translate contentKey="applicationConfiguration.chain.snapshotSync.fields.startingSubId.help">
                    Starting subscription ID for synchronization
                  </Translate>
                </FormText>
                {errors.chain?.snapshotSync?.startingSubId && <FormText color="danger">{errors.chain.snapshotSync.startingSubId}</FormText>}
              </FormGroup>
            </Col>
            <Col md={6}>
              <FormGroup>
                <Label for="syncPeriod">
                  <Translate contentKey="applicationConfiguration.chain.snapshotSync.fields.syncPeriod.label">Sync Period</Translate>
                </Label>
                <Input
                  type="number"
                  name="syncPeriod"
                  id="syncPeriod"
                  value={config.chain?.snapshotSync?.syncPeriod || 300.0}
                  onChange={e => handleSnapshotSyncChange('syncPeriod', parseFloat(e.target.value))}
                  step="0.1"
                  min="0.1"
                  invalid={!!errors.chain?.snapshotSync?.syncPeriod}
                />
                <FormText color="muted">
                  <Translate contentKey="applicationConfiguration.chain.snapshotSync.fields.syncPeriod.help">
                    Time between sync cycles (seconds)
                  </Translate>
                </FormText>
                {errors.chain?.snapshotSync?.syncPeriod && <FormText color="danger">{errors.chain.snapshotSync.syncPeriod}</FormText>}
              </FormGroup>
            </Col>
          </Row>
        </CardBody>
      </Card>
    </div>
  );
};

export default ChainConfigPanel;
