import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { Container, Row, Col, Nav, NavItem, NavLink, TabContent, TabPane, Card, CardHeader, CardBody, Button, Alert } from 'reactstrap';
import { Translate, translate } from 'react-jhipster';
import { toast } from 'react-toastify';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { ApplicationProperties } from 'app/shared/model/application-configuration.model';

// Panel components
import GeneralConfigPanel from './panels/general-config-panel';
import ServerConfigPanel from './panels/server-config-panel';
import ChainConfigPanel from './panels/chain-config-panel';
import ContainerConfigPanel from './panels/container-config-panel';

export const ApplicationConfiguration = () => {
  const navigate = useNavigate();
  const params = useParams();
  const location = useLocation();

  const [activeTab, setActiveTab] = useState('general');
  const [config, setConfig] = useState<ApplicationProperties>({
    configFilePath: '',
    forwardStats: true,
    manageContainers: true,
    startupWait: 5.0,
    server: {
      port: 8080,
      rateLimit: {
        numRequests: 100,
        period: 60,
      },
    },
    chain: {
      enabled: true,
      rpcUrl: 'http://localhost:8545',
      trailHeadBlocks: 10,
      routerAddress: '',
      wallet: {
        maxGasLimit: 1000000,
        privateKey: '',
        paymentAddress: '',
        allowedSimErrors: [],
      },
      snapshotSync: {
        sleep: 1.0,
        batchSize: 100,
        startingSubId: 0,
        syncPeriod: 300.0,
      },
    },
    docker: {
      username: '',
      password: '',
    },
    containers: [],
  });
  const [errors, setErrors] = useState<any>({});
  const [isDirty, setIsDirty] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  const dispatch = useAppDispatch();

  useEffect(() => {
    loadConfiguration();
  }, []);

  const loadConfiguration = async () => {
    setIsLoading(true);
    try {
      // API 호출로 설정 로드
      const response = await fetch('/api/application-configuration');
      if (response.ok) {
        const data = await response.json();
        setConfig(data);
      } else {
        // 기본값 사용
        setConfig({
          configFilePath: 'config/application-runtime.json',
          forwardStats: true,
          manageContainers: true,
          startupWait: 5.0,
          server: {
            port: 8080,
            rateLimit: {
              numRequests: 100,
              period: 60,
            },
          },
          chain: {
            enabled: true,
            rpcUrl: 'http://localhost:8545',
            trailHeadBlocks: 10,
            routerAddress: '',
            wallet: {
              maxGasLimit: 1000000,
              privateKey: '',
              paymentAddress: '',
              allowedSimErrors: [],
            },
            snapshotSync: {
              sleep: 1.0,
              batchSize: 100,
              startingSubId: 0,
              syncPeriod: 300.0,
            },
          },
          docker: {
            username: '',
            password: '',
          },
          containers: [
            {
              id: 'test-container',
              image: 'noosphere/test-container:latest',
              url: 'http://localhost:8001',
              bearer: 'your-bearer-token',
              port: 8001,
              external: false,
              gpu: false,
              acceptedPayments: {
                ETH: 1000000,
                USDC: 100000,
              },
              allowedIps: ['127.0.0.1'],
              allowedAddresses: [],
              allowedDelegateAddresses: [],
              description: 'test container',
              command: 'python server.py',
              env: {
                MODEL_PATH: '/models/test',
                BATCH_SIZE: '4',
              },
              generatesProofs: false,
              volumes: ['/models:/models', '/cache:/cache'],
            },
          ],
        });
      }
    } catch (error) {
      console.error('Failed to load configuration:', error);
      toast.error(translate('applicationConfiguration.error.loadFailed'));
    } finally {
      setIsLoading(false);
    }
  };

  const handleConfigChange = (newConfig: ApplicationProperties) => {
    setConfig(newConfig);
    setIsDirty(true);
  };

  const handleSave = async () => {
    setIsSaving(true);
    setErrors({});

    try {
      // 유효성 검사
      const validationErrors = validateConfiguration(config);
      if (Object.keys(validationErrors).length > 0) {
        setErrors(validationErrors);
        toast.error(translate('applicationConfiguration.error.validationFailed'));
        return;
      }

      // API 호출로 설정 저장
      const response = await fetch('/api/application-configuration', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(config),
      });

      if (!response.ok) {
        throw new Error('Failed to save configuration');
      }

      setIsDirty(false);
      toast.success(translate('applicationConfiguration.success.saved'));
    } catch (error) {
      console.error('Failed to save configuration:', error);
      toast.error(translate('applicationConfiguration.error.saveFailed'));
    } finally {
      setIsSaving(false);
    }
  };

  const handleReset = () => {
    loadConfiguration();
    setIsDirty(false);
    setErrors({});
  };

  const validateConfiguration = (configToValidate: ApplicationProperties): any => {
    const validationErrors: any = {};

    // ConfigFilePath validation
    if (!configToValidate.configFilePath) {
      validationErrors.configFilePath = 'Config file path is required';
    }

    // StartupWait validation
    if (configToValidate.startupWait <= 0) {
      validationErrors.startupWait = 'Startup wait must be greater than 0';
    }

    // Server validation
    if (configToValidate.server.port < 1 || configToValidate.server.port > 65535) {
      validationErrors.server = { ...validationErrors.server, port: 'Port must be between 1 and 65535' };
    }

    if (configToValidate.server.rateLimit.numRequests <= 0) {
      validationErrors.server = { ...validationErrors.server, numRequests: 'Number of requests must be greater than 0' };
    }

    if (configToValidate.server.rateLimit.period <= 0) {
      validationErrors.server = { ...validationErrors.server, period: 'Period must be greater than 0' };
    }

    // Chain validation
    if (configToValidate.chain.enabled) {
      if (!configToValidate.chain.rpcUrl) {
        validationErrors.chain = { ...validationErrors.chain, rpcUrl: 'RPC URL is required when chain is enabled' };
      }

      if (configToValidate.chain.trailHeadBlocks <= 0) {
        validationErrors.chain = { ...validationErrors.chain, trailHeadBlocks: 'Trail head blocks must be greater than 0' };
      }

      if (configToValidate.chain.wallet) {
        if (configToValidate.chain.wallet.maxGasLimit <= 0) {
          validationErrors.chain = {
            ...validationErrors.chain,
            wallet: { ...validationErrors.chain?.wallet, maxGasLimit: 'Max gas limit must be greater than 0' },
          };
        }
      }

      if (configToValidate.chain.snapshotSync) {
        if (configToValidate.chain.snapshotSync.sleep <= 0) {
          validationErrors.chain = {
            ...validationErrors.chain,
            snapshotSync: { ...validationErrors.chain?.snapshotSync, sleep: 'Sleep must be greater than 0' },
          };
        }

        if (configToValidate.chain.snapshotSync.batchSize <= 0) {
          validationErrors.chain = {
            ...validationErrors.chain,
            snapshotSync: { ...validationErrors.chain?.snapshotSync, batchSize: 'Batch size must be greater than 0' },
          };
        }
      }
    }

    // Containers validation
    configToValidate.containers.forEach((container, index) => {
      const containerErrors: any = {};

      if (!container.id) {
        containerErrors.id = 'Container ID is required';
      }

      if (!container.image) {
        containerErrors.image = 'Container image is required';
      }

      if (!container.url) {
        containerErrors.url = 'Container URL is required';
      }

      if (!container.bearer) {
        containerErrors.bearer = 'Bearer token is required';
      }

      if (container.port < 1 || container.port > 65535) {
        containerErrors.port = 'Port must be between 1 and 65535';
      }

      if (Object.keys(containerErrors).length > 0) {
        validationErrors.containers = validationErrors.containers || {};
        validationErrors.containers[index] = containerErrors;
      }
    });

    return validationErrors;
  };

  const toggle = (tab: string) => {
    if (activeTab !== tab) {
      setActiveTab(tab);
    }
  };

  if (isLoading) {
    return (
      <div className="d-flex justify-content-center">
        <div className="spinner-border" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="application-configuration">
      <Container fluid>
        <Row>
          <Col>
            <div className="d-flex justify-content-between align-items-center mb-4">
              <h2>
                <Translate contentKey="applicationConfiguration.title">Application Configuration</Translate>
              </h2>
              <div>
                <Button color="secondary" onClick={handleReset} disabled={!isDirty || isSaving} className="me-2">
                  <Translate contentKey="entity.action.reset">Reset</Translate>
                </Button>
                <Button color="primary" onClick={handleSave} disabled={!isDirty || isSaving}>
                  {isSaving ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                      <Translate contentKey="entity.action.saving">Saving...</Translate>
                    </>
                  ) : (
                    <Translate contentKey="entity.action.save">Save</Translate>
                  )}
                </Button>
              </div>
            </div>

            {isDirty && (
              <Alert color="warning" className="mb-4">
                <Translate contentKey="applicationConfiguration.unsavedChanges">
                  You have unsaved changes. Don&apos;t forget to save your configuration.
                </Translate>
              </Alert>
            )}

            <Card>
              <CardHeader>
                <Nav tabs>
                  <NavItem>
                    <NavLink className={activeTab === 'general' ? 'active' : ''} onClick={() => toggle('general')}>
                      <Translate contentKey="applicationConfiguration.tabs.labels.general">General</Translate>
                    </NavLink>
                  </NavItem>
                  <NavItem>
                    <NavLink className={activeTab === 'server' ? 'active' : ''} onClick={() => toggle('server')}>
                      <Translate contentKey="applicationConfiguration.tabs.labels.server">Server</Translate>
                    </NavLink>
                  </NavItem>
                  <NavItem>
                    <NavLink className={activeTab === 'chain' ? 'active' : ''} onClick={() => toggle('chain')}>
                      <Translate contentKey="applicationConfiguration.tabs.labels.chain">Chain</Translate>
                    </NavLink>
                  </NavItem>
                  <NavItem>
                    <NavLink className={activeTab === 'containers' ? 'active' : ''} onClick={() => toggle('containers')}>
                      <Translate contentKey="applicationConfiguration.tabs.labels.containers">Containers</Translate>
                    </NavLink>
                  </NavItem>
                </Nav>
              </CardHeader>
              <CardBody>
                <TabContent activeTab={activeTab}>
                  <TabPane tabId="general">
                    <GeneralConfigPanel config={config} onChange={handleConfigChange} errors={errors} />
                  </TabPane>
                  <TabPane tabId="server">
                    <ServerConfigPanel config={config} onChange={handleConfigChange} errors={errors} />
                  </TabPane>
                  <TabPane tabId="chain">
                    <ChainConfigPanel config={config} onChange={handleConfigChange} errors={errors} />
                  </TabPane>
                  <TabPane tabId="containers">
                    <ContainerConfigPanel config={config} onChange={handleConfigChange} errors={errors} />
                  </TabPane>
                </TabContent>
              </CardBody>
            </Card>
          </Col>
        </Row>
      </Container>
    </div>
  );
};

export default ApplicationConfiguration;
