import React from 'react';
import { FormGroup, Label, Input, FormText, Row, Col, Card, CardHeader, CardBody } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { ApplicationProperties } from 'app/shared/model/application-configuration.model';

interface GeneralConfigPanelProps {
  config: ApplicationProperties;
  onChange: (config: ApplicationProperties) => void;
  errors?: any;
}

const GeneralConfigPanel: React.FC<GeneralConfigPanelProps> = ({ config, onChange, errors = {} }) => {
  const handleChange = (field: string, value: any) => {
    onChange({
      ...config,
      [field]: value,
    });
  };

  return (
    <div className="general-config-panel">
      <Card className="mb-4">
        <CardHeader>
          <h5 className="mb-0">
            <Translate contentKey="applicationConfiguration.general.title">General Settings</Translate>
          </h5>
        </CardHeader>
        <CardBody>
          <Row>
            <Col md={6}>
              <FormGroup>
                <Label for="configFilePath">
                  <Translate contentKey="applicationConfiguration.general.fields.configFilePath.label">Config File Path</Translate>
                </Label>
                <Input
                  type="text"
                  name="configFilePath"
                  id="configFilePath"
                  value={config.configFilePath || ''}
                  onChange={e => handleChange('configFilePath', e.target.value)}
                  invalid={!!errors.configFilePath}
                />
                <FormText color="muted">
                  <Translate contentKey="applicationConfiguration.general.fields.configFilePath.help">
                    Path to the configuration file
                  </Translate>
                </FormText>
                {errors.configFilePath && <FormText color="danger">{errors.configFilePath}</FormText>}
              </FormGroup>
            </Col>
            <Col md={6}>
              <FormGroup>
                <Label for="startupWait">
                  <Translate contentKey="applicationConfiguration.general.fields.startupWait.label">Startup Wait Time</Translate>
                </Label>
                <Input
                  type="number"
                  name="startupWait"
                  id="startupWait"
                  value={config.startupWait || 0}
                  onChange={e => handleChange('startupWait', parseFloat(e.target.value))}
                  step="0.1"
                  min="0"
                  invalid={!!errors.startupWait}
                />
                <FormText color="muted">
                  <Translate contentKey="applicationConfiguration.general.fields.startupWait.help">
                    Time to wait before starting services (seconds)
                  </Translate>
                </FormText>
                {errors.startupWait && <FormText color="danger">{errors.startupWait}</FormText>}
              </FormGroup>
            </Col>
          </Row>
          <Row>
            <Col md={6}>
              <FormGroup check>
                <Label check>
                  <Input
                    type="checkbox"
                    checked={config.forwardStats || false}
                    onChange={e => handleChange('forwardStats', e.target.checked)}
                  />{' '}
                  <Translate contentKey="applicationConfiguration.general.fields.forwardStats.label">Forward Statistics</Translate>
                </Label>
                <FormText color="muted">
                  <Translate contentKey="applicationConfiguration.general.fields.forwardStats.help">Enable statistics forwarding</Translate>
                </FormText>
              </FormGroup>
            </Col>
            <Col md={6}>
              <FormGroup check>
                <Label check>
                  <Input
                    type="checkbox"
                    checked={config.manageContainers || false}
                    onChange={e => handleChange('manageContainers', e.target.checked)}
                  />{' '}
                  <Translate contentKey="applicationConfiguration.general.fields.manageContainers.label">Manage Containers</Translate>
                </Label>
                <FormText color="muted">
                  <Translate contentKey="applicationConfiguration.general.fields.manageContainers.help">
                    Enable automatic container management
                  </Translate>
                </FormText>
              </FormGroup>
            </Col>
          </Row>
        </CardBody>
      </Card>
    </div>
  );
};

export default GeneralConfigPanel;
